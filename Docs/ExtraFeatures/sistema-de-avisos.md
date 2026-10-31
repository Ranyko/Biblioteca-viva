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

## 5. Contrato HTTP

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

## 6. Regras de negócio

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

## 7. Fora de escopo

E-mail/SMS/WhatsApp/push (H17 segue `Won't` para canais externos) · aviso em tempo real (websocket/SSE) ·
preferências por usuário (aqui são globais) · régua escalonada (D-3/D-7) · expurgo automático (>180 dias —
declarado, não implementado) · consulta/baixa de multa (H11) · API de configuração (H13) · lock distribuído
(`pg_advisory_lock`) para multi-instância · **expiração de reserva** (`reserva.status = 'expirada'` +
reoferta/liberação do exemplar — regra de domínio de circulação; esta feature só **detecta** o fato e emite o
aviso, Decisão A).