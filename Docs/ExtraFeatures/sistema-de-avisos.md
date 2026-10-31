## 1. Contexto

O domínio da Biblioteca Viva **produz todos os fatos relevantes e não avisa ninguém**: uma devolução
disponibiliza a reserva do próximo da fila, uma multa nasce, um prazo de retirada de 2 dias corre — e o leitor
só descobre na próxima visita ao balcão. Consequência concreta:

| Situação                            | O que acontece hoje                    | Custo                                            |
| ----------------------------------- | -------------------------------------- | ------------------------------------------------ |
| Devolução vence amanhã              | Ninguém é avisado                      | Multa desnecessária                              |
| Exemplar reservado ficou disponível | Só o atendente vê, se consultar a fila | Prazo de 2 dias expira → reserva vira `expirada` |
| Multa gerada na devolução           | Leitor descobre na próxima visita      | Atrito no balcão                                 |
| Empréstimos atrasados de hoje       | Atendente consulta manualmente         | Trabalho repetido todo dia                       |

Segundo a seção 5 do documento `Docs/E1-E4/documento-de-visao-biblioteca-viva (E1).md` está fora do escopo `Envio de avisos por WhatsApp, SMS ou e-mail.`. Logo esta feature será implementanda via canal internal sem integrações SMTP, WhatsApp API ou Websocket.

**Resultado esperado:** leitor vê avisos de vencimento, reserva, fila e multa numa central paginada com badge de
não lidos; atendente enxerga as pendências do dia; admin liga/desliga tipos. Nada disso altera o comportamento
do domínio de circulação.

---

## 2. Escopo

| #   | Decisão                                                                                          |
| --- | ------------------------------------------------------------------------------------------------ |
| 1   | **Backend + frontend.** Notificação via **API REST consultada sob demanda** (sem push/websocket) |
| 2   | **Todos os 7 tipos** do catálogo geram aviso, incluindo `FILA_POSICAO` e `RESERVA_EXPIRADA`.     |
| 3   | `VENCIDO` é **lembrete diário** (`dia_referencia = hoje`, ≤ 1/dia por empréstimo).               |
| 4   | A **decisão sobre scheduler** está explícita e justificada (seção 4).                            |

---

## 3. Decisões de arquitetura

### A. `RESERVA_EXPIRADA` — a central apenas detecta o fato, não o produz

A expiração da reserva (`reserva.status = 'expirada'` + reoferta/liberação do exemplar) é **regra de domínio de
circulação**, fora do escopo desta feature — o escopo aqui é **somente o mecanismo de avisos**. A central é
somente leitura: a varredura emite `RESERVA_EXPIRADA` para reservas que **já estão** `expirada` no banco,
detectando o fato sem nunca escrever estado de negócio (regra 2 da seção 8).

> **Nota:** como hoje nada na `main` produz reservas `expirada` (a transição pertence ao domínio de circulação,
> ver seção 13), este tipo só é exercitável via fixture (ver seção 9). A feature **não implementa** a expiração
> — apenas o aviso. Declarado para não virar "implementação de H10 por acidente".

### B. `FILA_POSICAO` — persistir a última posição observada

A posição é derivada (`ORDER BY data_solicitacao, id`; `PROJETO.md` §6.2). Sem memória, "a posição diminuiu" é
indetectável. **Decisão:** `ALTER TABLE reserva ADD COLUMN ultima_posicao_observada SMALLINT` (aditiva,
nullable — não afeta inserts existentes). Por obra, sobre as reservas `ativa` ordenadas:

- `NULL` → grava a posição como baseline, **sem aviso** (evita notificar a entrada na fila como se fosse subida);
- posição atual **<** gravada → emite `FILA_POSICAO` e grava a nova;
- posição atual **≥** gravada → apenas grava.

A auto-atualização da baseline faz o índice único bastar: repetir a varredura no mesmo dia dá `p == baseline`
→ não emite de novo.

### C. Idempotência dos 7 tipos — `NULLS NOT DISTINCT` fecha o furo do `NULL`

O doc §5 aceita que `dia_referencia = NULL` anula o índice nos síncronos ("a garantia de banco não existe").
Isso é um furo evitável: o Postgres é **15**, que suporta **`NULLS NOT DISTINCT`**. Com ele, os 7 tipos ficam
protegidos pelo banco — a promessa central da feature ("o índice é a peça central, não um detalhe").

`ux_aviso_idempotente (usuario_id, tipo, referencia_tipo, referencia_id, dia_referencia) NULLS NOT DISTINCT`

| Tipo                 | `dia_referencia` | `referencia_tipo` / `referencia_id` | Garantia                                               |
| -------------------- | ---------------- | ----------------------------------- | ------------------------------------------------------ |
| `DEVIDA_AMANHA`      | hoje             | `emprestimo` / emprestimo_id        | **Banco**                                              |
| `VENCIDO`            | hoje             | `emprestimo` / emprestimo_id        | **Banco** (1/dia — lembrete diário deliberado)         |
| `RESERVA_EXPIRANDO`  | hoje             | `reserva` / reserva_id              | **Banco** (evita spam nos 2 dias de prazo)             |
| `RESERVA_EXPIRADA`   | hoje             | `reserva` / reserva_id              | **Banco**                                              |
| `FILA_POSICAO`       | hoje             | `reserva` / reserva_id              | **Banco** (≤ 1/dia)                                    |
| `RESERVA_DISPONIVEL` | `NULL`           | `reserva` / reserva_id              | **Banco** + aplicação (o gatilho é único)              |
| `MULTA_GERADA`       | `NULL`           | `multa` / multa_id                  | **Banco** + aplicação (`multa.emprestimo_id` é UNIQUE) |

**Onde o índice não protege:** ele não distingue fatos legítimos e distintos para o mesmo alvo — p.ex.
`DEVIDA_AMANHA` e `VENCIDO` do mesmo empréstimo convivem (tipo difere), e isso é correto. A idempotência "real"
dos síncronos continua apoiada em o gatilho ser único na operação de negócio; o índice é a rede de segurança.

---

## 4. Decisão sobre o scheduler

**Decisão: usar `@Scheduled`** — um artefato próprio no mesmo
repositório, que comparte as classes de domínio/repositórios e o mesmo banco. Config com
`@Scheduled(cron = "${app.avisos.varredura-cron:0 0 6 * * *}", zone = "${app.biblioteca.zone-id:America/Sao_Paulo}")`
e `@ConditionalOnProperty("app.avisos.varredura-habilitada")`. A API mantém o `POST /admin/avisos/varredura`
(admin) como gatilho manual, executando a **mesma** lógica de varredura (lógica compartilhada, deploy distinto).

| Critério        | Por quê                                                                                                                                                                                                                                                                                              |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Necessidade     | **5 dos 7 tipos dependem da passagem do tempo**: `DEVIDA_AMANHA`, `VENCIDO`, `RESERVA_EXPIRANDO`, `RESERVA_EXPIRADA` e `FILA_POSICAO` só são detectáveis comparando as datas com o "hoje" (`Clock`). Não existe evento de usuário para _"hoje é o último dia"_ — datas "chegam" sem ação de ninguém. |
| Multi-instância | Se o job rodar em mais de um processo, o índice único + `ON CONFLICT DO NOTHING` torna a duplicação inofensiva.                                                                                                                                                                                      |
| Testabilidade   | Testes **não** dependem do cron: chamam o service (unitário, `Clock.fixed`) ou o endpoint manual (integração). O agendador é wrapper fino.                                                                                                                                                           |
| Operação        | Botão "gerar avisos agora" (na API) + log estruturado de início/fim com contagem (no processo do job).                                                                                                                                                                                               |

---

## 5. Modelo de dados — `V4__central_de_avisos.sql`

Aditiva (não altera `V1`–`V3`), `snake_case` singular, `CHECK` em vez de ENUM nativo, `NULLS NOT DISTINCT`
(exige PG15 — alvo confirmado; declarar no cabeçalho do arquivo).

```sql
ALTER TABLE reserva                                  -- Decisão B
    ADD COLUMN ultima_posicao_observada SMALLINT,
    ADD CONSTRAINT chk_reserva_ultima_posicao
        CHECK (ultima_posicao_observada IS NULL OR ultima_posicao_observada > 0);

CREATE TABLE aviso (
    id              BIGSERIAL   NOT NULL,
    usuario_id      BIGINT      NOT NULL,
    tipo            VARCHAR(30) NOT NULL,
    titulo          VARCHAR(120) NOT NULL,
    mensagem        VARCHAR(500) NOT NULL,
    referencia_tipo VARCHAR(30) NULL,
    referencia_id   BIGINT      NULL,
    dia_referencia  DATE        NULL,
    criado_em       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lido_em         TIMESTAMPTZ NULL,
    CONSTRAINT pk_aviso PRIMARY KEY (id),
    CONSTRAINT fk_aviso_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_aviso_tipo CHECK (tipo IN (
        'DEVIDA_AMANHA','VENCIDO','RESERVA_DISPONIVEL','RESERVA_EXPIRANDO',
        'RESERVA_EXPIRADA','MULTA_GERADA','FILA_POSICAO')),
    CONSTRAINT chk_aviso_referencia CHECK (
        (referencia_tipo IS NULL AND referencia_id IS NULL)
     OR (referencia_tipo IN ('emprestimo','reserva','multa') AND referencia_id IS NOT NULL)),
    CONSTRAINT chk_aviso_leitura CHECK (lido_em IS NULL OR lido_em >= criado_em)
);

CREATE UNIQUE INDEX ux_aviso_idempotente             -- Decisão C
    ON aviso(usuario_id, tipo, referencia_tipo, referencia_id, dia_referencia)
    NULLS NOT DISTINCT;

CREATE INDEX ix_aviso_usuario_nao_lido ON aviso(usuario_id, criado_em DESC) WHERE lido_em IS NULL;
CREATE INDEX ix_aviso_usuario_criado   ON aviso(usuario_id, criado_em DESC);

CREATE TABLE aviso_preferencia (                     -- linha única, padrão configuracao_biblioteca
    id                 SMALLINT NOT NULL,
    devida_amanha      BOOLEAN  NOT NULL DEFAULT TRUE,
    vencido            BOOLEAN  NOT NULL DEFAULT TRUE,
    reserva_disponivel BOOLEAN  NOT NULL DEFAULT TRUE,
    reserva_expirando  BOOLEAN  NOT NULL DEFAULT TRUE,
    reserva_expirada   BOOLEAN  NOT NULL DEFAULT TRUE,
    multa_gerada       BOOLEAN  NOT NULL DEFAULT TRUE,
    fila_posicao       BOOLEAN  NOT NULL DEFAULT FALSE,   -- nasce desligado (anti-spam)
    atualizado_em      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_aviso_preferencia PRIMARY KEY (id),
    CONSTRAINT chk_aviso_preferencia_unica CHECK (id = 1)
);
INSERT INTO aviso_preferencia (id) VALUES (1);
```

> `fila_posicao` nasce **desligado** por anti-spam, mas o tipo é implementado e testado (decisão 2) — a tela de
> preferências do admin liga.
>
> **Nota de implementação:** como `criado_em` e `lido_em` são gravados a partir do `Clock` injetado (e não do
> `DEFAULT CURRENT_TIMESTAMP`), a CHECK `chk_aviso_leitura` permanece válida mesmo nos testes com relógio fixo.

---

## 6. Camadas e arquivos

### Backend — novos (`src/main/java/br/com/bibliotecaviva/…`)

| Arquivo                                                                                                                                                | Papel                                                                                                                                                                                                                                                                                               |
| ------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `model/Aviso.java`                                                                                                                                 | Entidade e seus respectivos enums                                                                                                                      |
| `repository/AvisoRepository.java`                                                                                                                      | `JdbcTemplate` + records: `inserirSeAusente(...)` (`ON CONFLICT DO NOTHING RETURNING id`), listagem+contagem paginada, `contarNaoLidos`, `marcarLido` (retorna linhas afetadas → 0 = 404), `marcarTodosLidos`, `preferencias()`/`atualizarPreferencias(...)`, queries da varredura.                 |
| `service/AvisoService.java`                                                                                                                            | Geração pontual + leitura. `criar(...)` checa preferência e usuário ativo; listagem/contagem/leitura/preferências com validação de paginação.                                                                                                                                                       |
| `service/AvisoVarreduraService.java`                                                                                                                   | Orquestra a varredura: emite `DEVIDA_AMANHA`, `VENCIDO`, `RESERVA_EXPIRANDO`, `RESERVA_EXPIRADA`, `FILA_POSICAO` — para `RESERVA_EXPIRADA`, apenas detecta reservas **já** `expirada` no banco (Decisão A, sem escrever estado de negócio). Devolve `VarreduraResponse` com contagens. Usa `Clock`. |
| `config/AgendamentoConfig.java`                                                                                                                        | `@EnableScheduling`.                                                                                                                                                                                                                                                                                |
| `scheduler/AvisoVarreduraAgendador.java`                                                                                                               | `@Scheduled` property-gated; só chama `AvisoVarreduraService.executar()`.                                                                                                                                                                                                                           |
| `controller/AvisoLeitorController.java`                                                                                                                | `@RequestMapping("/leitor/avisos")` — GET lista, PATCH leitura, POST leitura-todos.                                                                                                                                                                                                                 |
| `controller/AvisoContagemController.java`                                                                                                              | `@RequestMapping("/avisos/nao-lidos")` — GET contagem (autenticado).                                                                                                                                                                                                                                |
| `controller/AvisoAtendenteController.java`                                                                                                             | `@RequestMapping("/atendente/avisos")` — GET pendências.                                                                                                                                                                                                                                            |
| `controller/AvisoAdminController.java`                                                                                                                 | `@RequestMapping("/admin/avisos")` — GET/PUT preferências, POST varredura.                                                                                                                                                                                                                          |
| `dto/AvisoResponse`, `PreferenciaRequest`, `PreferenciaResponse`, `PendenciasResponse`, `ContagemResponse`, `AtualizacaoResponse`, `VarreduraResponse` | records.                                                                                                                                                                                                                                                                                            |

### Backend — alterados

| Arquivo                                     | Alteração                                                                                                                          |
| ------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `service/CirculacaoService.java`            | Ganchos em `devolver` (após os writes de negócio): `RESERVA_DISPONIVEL` e `MULTA_GERADA`. Injeta `AvisoService`/`AvisoRepository`. |
| `repository/CirculacaoRepository.java`      | `gerarMulta` passa a `RETURNING id` e retorna `long` (para `referencia_id`).                                                       |
| `src/main/resources/application.properties` | `app.avisos.varredura-cron`, `app.avisos.varredura-habilitada`.                                                                    |
| `src/test/resources/sprint2-fixture.sql`    | Ver seção 9.                                                                                                                       |

> **Trade-off declarado:** o gancho fica **dentro** da transação de `devolver` (como o doc §8 pede) — se a
> inserção do aviso falhar, a devolução reverte. É coerente com `falhaNaMultaReverteDevolucaoEStatus`
> (`CirculacaoPostgresTest:268`), que já codifica essa aceitação. Alternativa futura (não nesta entrega):
> `REQUIRES_NEW` ou evento pós-commit.

### Frontend — branch `origin/frontend-sprint-3` (`frontend/src/`)

| Arquivo                       | Papel                                                                                                                                                     |
| ----------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `services/avisos.ts`          | Tipos + `avisosService` (listar, marcarLido, marcarTodos, contagem, pendencias, preferencias, atualizar) sobre `api<T>()` e `Pagina<T>` de `catalogo.ts`. |
| `components/SinoAvisos.tsx`   | Badge; busca a contagem **uma vez por navegação** (`useEffect` com `location.pathname`) → `/avisos`.                                                      |
| `components/Navbar.tsx`       | Renderiza `<SinoAvisos/>` no `nav-right` (só autenticado).                                                                                                |
| `pages/CentralAvisos.tsx`     | Lista paginada, filtro lido/não lido, "marcar todos".                                                                                                     |
| `pages/PendenciasDoDia.tsx`   | Atrasados + reservas expirando.                                                                                                                           |
| `pages/PreferenciasAviso.tsx` | Toggles por tipo.                                                                                                                                         |
| `auth/perfis.ts`              | Item "Avisos" (leitor → `/avisos`), "Pendências" (atendente → `/atendente/pendencias`), "Avisos" (admin → `/admin/avisos`).                               |
| `App.tsx`                     | Rotas com `RequireRole`.                                                                                                                                  |

> **Correção ao doc §7:** o doc põe a central `/avisos` como "todos", mas o endpoint de dados é
> `/leitor/avisos`, que o `SecurityConfig` restringe a `ROLE_LEITOR` — "todos" geraria 403 para
> atendente/admin. **Restringir a central ao `leitor`**; atendente e admin usam o sino (que bate em
> `/avisos/nao-lidos/contagem`, `authenticated()`) e suas telas próprias.

---

## 7. Contrato HTTP

Nenhuma mudança no `SecurityConfig` — todos os paths herdam o RBAC por prefixo.

| Método | Path                                                 | Auth            | Resposta                            | Erros                  |
| ------ | ---------------------------------------------------- | --------------- | ----------------------------------- | ---------------------- |
| GET    | `/leitor/avisos` (`lido?`, `pagina`=0, `tamanho`=20) | leitor          | 200 `PaginaResponse<AvisoResponse>` | 400 paginação          |
| PATCH  | `/leitor/avisos/{id}/leitura`                        | leitor          | 200 `AvisoResponse`                 | 404 se de outro leitor |
| POST   | `/leitor/avisos/leitura-todos`                       | leitor          | 200 `{ "atualizados": n }`          | —                      |
| GET    | `/avisos/nao-lidos/contagem`                         | autenticado     | 200 `{ "naoLidos": n }`             | 401                    |
| GET    | `/atendente/avisos/pendencias`                       | atendente/admin | 200 `PendenciasResponse`            | —                      |
| GET    | `/admin/avisos/preferencias`                         | admin           | 200 `PreferenciaResponse`           | —                      |
| PUT    | `/admin/avisos/preferencias`                         | admin           | 200 `PreferenciaResponse`           | 400 validação          |
| POST   | `/admin/avisos/varredura`                            | admin           | 200 `VarreduraResponse`             | —                      |

`AvisoResponse(id, tipo, titulo, mensagem, referenciaTipo, referenciaId, criadoEm, lido, lidoEm)` — **nunca**
expõe `usuarioId`; o isolamento é pelo `@AuthenticationPrincipal Usuario`.

---

## 8. Regras de negócio

**Catálogo fechado de tipos** (enum Java, persistido em `VARCHAR` + CHECK — o projeto não usa ENUM nativo):

| Tipo                 | Gatilho                                                    | Momento                    | Destinatário |
| -------------------- | ---------------------------------------------------------- | -------------------------- | ------------ |
| `DEVIDA_AMANHA`      | empréstimo ativo com `data_prevista_devolucao = hoje + 1`  | varredura diária           | leitor       |
| `VENCIDO`            | empréstimo ativo com `data_prevista_devolucao < hoje`      | varredura diária           | leitor       |
| `RESERVA_DISPONIVEL` | devolução disponibilizou o exemplar                        | **síncrono**, na devolução | leitor       |
| `RESERVA_EXPIRANDO`  | reserva `disponivel` com `data_limite_retirada = hoje + 1` | varredura diária           | leitor       |
| `RESERVA_EXPIRADA`   | reserva **já** `expirada` no banco (Decisão A)             | varredura diária           | leitor       |
| `MULTA_GERADA`       | multa criada com valor > 0                                 | **síncrono**, na devolução | leitor       |
| `FILA_POSICAO`       | a posição do leitor na fila diminuiu                       | varredura diária           | leitor       |

**Regras transversais:**

1. **Idempotência obrigatória**, garantida no banco pelo índice único (Decisão C), não por lógica de aplicação.
2. **Aviso não altera estado de negócio.** A central é somente leitura sobre o domínio; `RESERVA_EXPIRADA`
   apenas detecta reservas **já** `expirada` no banco (Decisão A), nunca escreve estado de negócio.
3. **Nada de dado sensível no corpo do aviso.**
4. **Usuário inativo não recebe aviso novo** (coerente com RNF10).
5. **Sem push, sem e-mail.**
6. **Retenção:** avisos lidos com mais de 180 dias podem ser expurgados por rotina futura — declarado, **não**
   implementado.

---

## 9. Plano de testes

**Unitários** (Mockito + `Clock.fixed`, padrão `CirculacaoServiceTest`):

- `AvisoVarreduraServiceTest`: `DEVIDA_AMANHA` p/ vencimento hoje+1, **não** p/ hoje+2; `VENCIDO` p/ atrasado;
  rodar 2× no mesmo dia → 1; tipo desligado → 0; usuário inativo → 0; `FILA_POSICAO` só quando a posição cai
  (e baseline sem aviso na 1ª observação); dia seguinte cria o 2º `VENCIDO` (deliberado); `RESERVA_EXPIRADA`
  emitido para reserva **já** `expirada` no banco (Decisão A).
- `CirculacaoServiceTest` (atualizado): `devolver` emite `RESERVA_DISPONIVEL` (com data limite no texto) e
  `MULTA_GERADA`; multa zero **não** emite (coerente com `tetoZeroNaoCriaMulta`); obra inativa não emite.
- `AvisoServiceTest`: marcar aviso de outro → 0 linhas → 404; paginação inválida → `IllegalArgumentException`.

**Integração** (`AvisoPostgresTest`, Postgres 15 real, gate `SPRINT2_TEST_DB_URL`):

- `ux_aviso_idempotente` rejeita 2ª inserção idêntica — **inclusive com `dia_referencia` NULL** (valida o
  `NULLS NOT DISTINCT`); `chk_aviso_tipo` e `chk_aviso_referencia` rejeitam valores inválidos.
- Varredura 2× no mesmo dia → 1 `VENCIDO`; no dia seguinte → 2.
- `POST /leitor/avisos/leitura-todos` não afeta avisos de outro usuário.
- `POST /admin/avisos/varredura` idempotente (2× → mesma contagem).
- Gancho em `devolver`: devolução com multa cria `MULTA_GERADA`; com fila cria `RESERVA_DISPONIVEL`.

**Autorização:** leitor lista os próprios (200) · leitor marca aviso de outro (404) · atendente em
`/leitor/avisos` (403) · admin em `/admin/avisos/preferencias` (200) · atendente em
`/admin/avisos/preferencias` (403) · leitor em `/admin/avisos/varredura` (403) · não autenticado em
`/avisos/nao-lidos/contagem` (401).

**Fixture `sprint2-fixture.sql`** — o que ela não cobre hoje:

- O TRUNCATE **não inclui** `aviso`/`aviso_preferencia`. Adicionar as duas e re-inserir
  `INSERT INTO aviso_preferencia (id) VALUES (1)`.
- Não há empréstimo ativo vencendo amanhã, nem atrasado, nem reserva `disponivel` com prazo. Adicionar dados
  **relativos ao `Clock` fixo** (`2026-10-01`): vencimento em `2026-10-02`, um já atrasado, e uma reserva
  `disponivel` com `data_limite_retirada` em `2026-10-02`.

**Frontend:** `npm run lint` + `npm run build`; verificação manual do badge e das telas por perfil.

---

## 10. Sequenciamento (fases verificáveis)

| Fase | Entrega                                                                | Pronto quando                                                                  |
| ---- | ---------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| 0    | `V4` + `TipoAviso` + `AvisoRepository`                                 | migration aplica em PG vazio; índice `NULLS NOT DISTINCT` e CHECKs validados   |
| 1    | `AvisoService` (leitura/preferências) + controllers de leitura + DTOs  | MockMvc 200/403/404; paginação; isolamento                                     |
| 2    | Ganchos em `CirculacaoService.devolver` + `gerarMulta` retornando id   | `CirculacaoServiceTest` atualizado verde; `CirculacaoPostgresTest` ainda verde |
| 3    | `AvisoVarreduraService` + agendamento + `POST /admin/avisos/varredura` | varredura idempotente (2×/dia = 1); agendador desligável por property          |
| 4    | `GET /atendente/avisos/pendencias`                                     | payload com atrasados + reservas expirando em ≤ 1 dia; RBAC                    |
| 5    | Frontend (service, sino, 3 telas, menu, rotas)                         | lint + build verdes; fluxo manual por perfil                                   |

> **Colisão de migration:** `V4` está livre hoje; se outra branch pegar `V4` primeiro, rebasear para `V5`
> (`Docs/ExtraFeats/README.md` §1). Combinar a numeração antes de codar.

---

## 11. Verificação end-to-end

1. `docker compose up -d` (Postgres 15 na 15432) e `./mvnw clean verify` — deve manter os 111 testes atuais verdes.
2. `SPRINT2_TEST_DB_URL=... ./mvnw test` para a integração do módulo.
3. Com token de admin: `POST /admin/avisos/varredura` e conferir
   `SELECT tipo, count(*) FROM aviso GROUP BY tipo`.
4. Com token de leitor: `GET /leitor/avisos`, `GET /avisos/nao-lidos/contagem`, marcar lido e conferir a queda.
5. Frontend: `npm run dev` na branch de frontend; conferir badge no `Navbar` e as 3 telas por perfil.

---

## 12. Riscos e mitigação

| Risco                                                          | Mitigação                                                                                                                                              |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Construtor de `CirculacaoService` muda (injeta `AvisoService`) | Atualizar `CirculacaoServiceTest` (mock do novo serviço) — mudança mecânica                                                                            |
| `gerarMulta` `void → long`                                     | Testes existentes usam `any()` no `verify` — não quebram                                                                                               |
| Hook na mesma transação reverte a devolução                    | Trade-off aceito e já codificado em `falhaNaMultaReverteDevolucaoEStatus`; documentar                                                                  |
| `NULLS NOT DISTINCT` exige PG15                                | Alvo confirmado (`docker-compose.yml`/CI = postgres:15); declarar no cabeçalho da V4                                                                   |
| `RESERVA_EXPIRADA` sem gatilho em produção                     | Declarar em `Docs/` que a feature **não** implementa a expiração (regra de domínio de circulação, fora de escopo); o tipo só é exercitável via fixture |
| Central `/avisos` "todos" geraria 403                          | Restringir ao `leitor` (correção ao doc §7)                                                                                                            |
| Fixture não trunca `aviso_preferencia`                         | Adicionar as duas tabelas ao TRUNCATE + re-seed                                                                                                        |
| Fuso na virada do dia                                          | `LocalDate.now(clock)`, nunca `now()`; teste com `Clock.fixed` perto da meia-noite                                                                     |
| Varredura duplicando avisos                                    | Índice único parcial + `ON CONFLICT DO NOTHING`                                                                                                        |
| Avisos demais ("spam")                                         | `FILA_POSICAO` nasce desligado; preferências por tipo                                                                                                  |
| Scheduler sem observabilidade                                  | Log estruturado de início/fim com contagem de avisos criados                                                                                           |

---

## 13. Fora de escopo

E-mail/SMS/WhatsApp/push (H17 segue `Won't` para canais externos) · aviso em tempo real (websocket/SSE) ·
preferências por usuário (aqui são globais) · régua escalonada (D-3/D-7) · expurgo automático (>180 dias —
declarado, não implementado) · consulta/baixa de multa (H11) · API de configuração (H13) · lock distribuído
(`pg_advisory_lock`) para multi-instância · **expiração de reserva** (`reserva.status = 'expirada'` +
reoferta/liberação do exemplar — regra de domínio de circulação; esta feature só **detecta** o fato e emite o
aviso, Decisão A).

---

## 14. Divergências em relação ao documento-fonte

Três correções, todas verificadas no código:

| #   | O que o `sistema-de-notificacao.md` diz                                                    | O que este plano propõe                                                                                                                                               | Base factual                                                                                                               |
| --- | ------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| 1   | §4.2/§4.6: a varredura poderia marcar `expirada`; ou não implementar o tipo                | **Não implementar a expiração** (regra de domínio de circulação, fora de escopo); a varredura só emite `RESERVA_EXPIRADA` para reservas **já** `expirada` (Decisão A) | `PROJETO.md:1022` (§8.5) já documenta a regra; nada a implementa na `main`; o escopo da feature é só o mecanismo de avisos |
| 2   | §5: `dia_referencia = NULL` anula o índice; "a garantia de banco não existe" nos síncronos | Usar `NULLS NOT DISTINCT` para proteger os 7 tipos                                                                                                                    | Postgres 15 em `docker-compose.yml` e no CI                                                                                |
| 3   | §7: central `/avisos` disponível para "todos"                                              | Restringir ao `leitor`                                                                                                                                                | `/leitor/avisos` é `ROLE_LEITOR` no `SecurityConfig`; "todos" geraria 403                                                  |

---

## 15. Status de implementação (backend)

Backend das fases 0–4 concluído e verde: **146 testes, 0 falhas** (`./mvnw clean test` com `SPRINT2_TEST_DB_URL`).

### 15.1 Divergências do plano §6 aceitas na implementação

| Plano §6 diz                                       | Implementado como                                                                 | Por quê                                                                                                             |
| -------------------------------------------------- | --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| 4 controllers (`AvisoLeitorController`, etc.)      | **1** `controller/AvisoController.java` com as 8 rotas em paths absolutos          | Decisão do dev: centralizar as rotas de avisos. Sem `@RequestMapping` de classe para herdar o RBAC por prefixo.      |
| `repository/AvisoRepository.java` (era o nome do doc) | `repository/AvisoRepository.java` **com o nome do doc** (nasceu como `AvisoConsultaRepository`) | Consolidado ao nome do §6.                                                            |
| `AvisoResponse(id, …, lido, lidoEm)`               | `AvisoResponse(id, …, lidoEm)` + `isLido()` derivado                               | Evita campo redundante; `lido` é `lidoEm != null`.                                                                  |

### 15.2 Camadas entregues

| Camada / arquivo                                                    | Status | Observação                                                                                          |
| ------------------------------------------------------------------- | ------ | --------------------------------------------------------------------------------------------------- |
| `db/migration/V4__central_de_avisos.sql`                            | ✅     | Corrigido: faltava `;` após `ALTER TABLE` e o `INSERT` da linha 1 de `aviso_preferencia`.            |
| `model/Aviso.java` (+ `TipoAviso`, `ReferenciaTipo`)                | ✅     | Corrigido: faltava a linha `package`.                                                               |
| `repository/AvisoRepository.java`                                   | ✅     | Listagem paginada, contagem, `marcarLido`/`marcarTodosLidos` (retornam `int`), `preferencias()`/`atualizarPreferencias`, `inserirSeAusente` (`ON CONFLICT DO NOTHING RETURNING`), `buscar`, queries de varredura e de pendências. |
| `service/AvisoService.java`                                         | ✅     | `criar(...)` (checa preferência + usuário ativo), `listar`/`contarNaoLidos`/`marcarLido`/`marcarTodosLidos`, `preferencias`/`atualizarPreferencias`, `pendencias`. |
| `service/AvisoVarreduraService.java`                                | ✅     | Emite os 5 tipos temporais; `RESERVA_EXPIRADA` só **detecta** (Decisão A); `FILA_POSICAO` com baseline em `ultima_posicao_observada`. |
| `controller/AvisoController.java`                                   | ✅     | 8 rotas do §7.                                                                                       |
| `config/AgendamentoConfig.java`                                     | ✅     | `@EnableScheduling`.                                                                                 |
| `scheduler/AvisoVarreduraAgendador.java`                            | ✅     | `@Scheduled` property-gated; log estruturado de início/fim com contagem.                              |
| `service/CirculacaoService.devolver`                                | ✅     | Ganchos síncronos `MULTA_GERADA` (valor > 0) e `RESERVA_DISPONIVEL`, na mesma transação.              |
| `repository/CirculacaoRepository.gerarMulta`                        | ✅     | Passou a `RETURNING id` → `long`.                                                                    |
| `application.properties`                                            | ✅     | `app.avisos.varredura-cron` (default `0 0 6 * * *`), `app.avisos.varredura-habilitada` (default `false`). |
| `test/resources/sprint2-fixture.sql`                                | ✅     | `TRUNCATE` inclui `aviso`/`aviso_preferencia` + re-seed da linha 1. Não adiciona empréstimo/reserva relativos ao `Clock` (quebraria as contagens de `CirculacaoPostgresTest`); `AvisoPostgresTest` insere os próprios dados. |

### 15.3 Testes adicionados

| Arquivo                              | Cobre                                                                                     |
| ------------------------------------ | ----------------------------------------------------------------------------------------- |
| `service/AvisoServiceTest`           | tipo desligado → 0; usuário inativo → 0; paginação inválida → `IllegalArgumentException`; aviso de outro → 404; idempotência de re-marcação. |
| `service/AvisoVarreduraServiceTest`  | `DEVIDA_AMANHA` só para hoje+1; `VENCIDO` para atrasado; baseline de fila sem aviso e emissão quando a posição cai; total. |
| `integration/AvisoPostgresTest`      | RBAC (401/403/200); preferências GET/PUT + 400; varredura 2× no mesmo dia = 1; isolamento do leitor; pendências. |
| `service/CirculacaoServiceTest`      | (atualizado) ganchos: multa emite aviso, multa zero não, fila emite, obra inativa não.      |

### 15.4 Pendências declaradas

- **Frontend** (§6, branch `origin/frontend-sprint-3`) — fora desta entrega.
- **Fixture relativa ao `Clock`** (§9) — não incluída pelo motivo acima; coberta por `AvisoPostgresTest`.
- **Expiração de reserva** — segue fora de escopo (§13, Decisão A): a feature só detecta `expirada`.

### 15.5 Correções após revisão (tier caro)

| # | Achado | Correção |
| - | ------ | -------- |
| R1 | `total` da paginação voltava `0` quando o `OFFSET` passava do fim (`COUNT(*) OVER()` some sem linhas) | Contagem separada (`SELECT count(*)`), como `AcervoConsultaRepository`. Teste `paginaForaDoIntervaloMantemTotal`. |
| R2 | `RESERVA_EXPIRADA` reemitia **todo dia** para toda reserva já expirada (sem evento que mude o status) | `dia_referencia = NULL` → 1 aviso por reserva, para sempre. **Desvio do §5** (que põe `hoje`), declarado no código. |
| R3 | `data_limite_retirada::date` dependia do `TimeZone` da sessão JDBC (default da JVM), não do fuso da biblioteca | `(data_limite_retirada AT TIME ZONE :zona)::date`, com `:zona` de `app.biblioteca.zone-id`. |
| M1 | `lido_em = CURRENT_TIMESTAMP` contradizia a nota do §5 (gravar a partir do `Clock`) | `marcarLido`/`marcarTodosLidos` recebem `OffsetDateTime.now(clock)`. |
| M2 | `GET /leitor/avisos` expunha `apenasNaoLidos` em vez do `lido?` do §7 | Parâmetro `lido` tri-estado (`null`=todos, `false`=não lidos, `true`=lidos). Teste `filtroLidoSeparaLidosDeNaoLidos`. |
| M4 | Posição da fila ignorava perfil/limite usados na alocação | `FILA_POSICOES_SQL` filtra `u.perfil='leitor'` e empréstimos ativos < `limite_emprestimos`, igual `primeiraReservaElegivel`. |
| M5 | `varrerFila` relia o relógio | Recebe `hoje` como os demais. |

**Não corrigidos (declarados):** M3 (`model/Aviso.java` é entidade JPA não usada — toda persistência é JDBC; mantida por ora, mas com duas fontes de tempo divergentes) e M6 (newline final, cosmético).
