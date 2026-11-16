# Relatório de Entrega — Sprint 1 — Biblioteca Viva

**Equipe:** Raniery Chiarelli (RA 2840482321007) — Vinicius Rocha (RA 2840482523051) — Isaac Leonardo da Silva (RA 2840482421016)

**Período planejado:** 12/09/2026 a 18/09/2026

**Datas dos testes registrados:** 23/09/2026 e 02/10/2026

**Última atualização deste relatório:** 02/10/2026

**Sprint Review:** não houve sprint review até o momento

## 1. Planejado vs. entregue

| História da E2 | Planejada para a Sprint 1? | Entregue? | Observação |
|---|---|---|---|
| #1 — Login e perfis | Sim | Backend entregue; integração não verificada | Login com JWT, BCrypt, três perfis e bloqueio de usuário inativo implementados e testados. A integração com a tela React não foi verificada nesta entrega. |
| #2 — Gerenciar usuários | Sim | API demonstrada; integração não verificada | Listagem, criação, atualização e inativação foram demonstradas; o acesso com o token anterior à inativação retornou HTTP 401. A integração com a tela React não foi verificada nesta entrega. |
| #3 — Cadastrar leitores | Sim | API demonstrada; integração não verificada | Cadastro retornou HTTP 201, sem senha/hash, e documento duplicado retornou HTTP 409. A integração com a tela não foi verificada nesta entrega. |
| #4 — Cadastrar livros, autores e categorias | Sim | Não | Tabelas presentes no banco, mas endpoints não implementados. Item replanejado para a Sprint 2. |
| #5 — Cadastrar exemplares | Sim | Não | Tabela e restrições presentes no banco, mas endpoints não implementados. Item replanejado para a Sprint 2. |

## 2. Incremento funcional demonstrável

A Sprint 1 produziu uma API Spring Boot conectada ao PostgreSQL e executada localmente. O incremento permite:

- autenticar usuários de demonstração e emitir JWT;
- diferenciar os perfis Leitor, Atendente e Administrador;
- retornar HTTP 401 para requisições não autenticadas;
- retornar HTTP 403 quando o perfil não possui autorização;
- permitir ao Administrador listar, criar e atualizar usuários;
- permitir ao Atendente ou Administrador listar e cadastrar leitores;
- aplicar migrations e seed pelo Flyway;
- executar 11 testes unitários e um roteiro com 9 verificações HTTP.

Passo a passo no macOS ou Linux:

```bash
docker compose up -d
./mvnw test
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
docker compose up -d
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

No Windows, o roteiro HTTP pode ser executado em outro terminal:

```powershell
.\scripts\testar-api.ps1
```

O procedimento completo, os usuários de demonstração e os endpoints estão no [README](../../../README.md). A demonstração desta sprint é local; o deploy público permanece planejado para uma etapa posterior.

No macOS, os fluxos HTTP foram executados no Postman com a [coleção da Sprint 1](../../Postman/sprint-1.postman_collection.json).

## 3. Backlog atualizado

- Backlog-base: [`Docs/E1-E4/backlog-biblioteca-viva(E2).md`](../../E1-E4/backlog-biblioteca-viva%28E2%29.md);
- branch do incremento: [`feature/backend-sprint-1`](https://github.com/Ranyko/Biblioteca-viva/tree/feature/backend-sprint-1);

Estados definidos no fechamento desta sprint:

- histórias #1, #2 e #3: backend implementado e fluxos registrados nas evidências; integração com frontend não verificada nesta entrega;
- histórias #4 e #5: replanejadas para a Sprint 2;
- demais histórias: permanecem nas sprints previstas.

### Organização atual da equipe

A distribuição abaixo foi informada por Raniery em 02/10/2026. Ela registra responsabilidades, sem afirmar que as entregas dos demais integrantes já foram concluídas ou verificadas.

| Frente | Responsável | Escopo |
|---|---|---|
| Backend da Sprint 1 | Raniery Chiarelli | H1–H3, dados, autenticação e autorização |
| Backend da Sprint 2 | Raniery Chiarelli | H4–H8: acervo, exemplares, pesquisa, empréstimos e devoluções |
| Histórias H9 e H10 da Sprint 2 | Vinicius Rocha | Reservas e consulta/gestão da fila; alinhar integração com empréstimos e devoluções |
| Frontend e integração com a API | Isaac Leonardo da Silva | Telas e consumo dos endpoints |
| Testes, conferência das entregas e revisão | Toda a equipe | Cada integrante valida sua implementação e participa dos fluxos integrados; resultados devem identificar quem realmente executou |
| Versionamento e organização atual desta entrega | Raniery Chiarelli | Git, branch do backend e consolidação dos documentos |

O acompanhamento ocorre pelos documentos versionados e pelas conversas do grupo. Este relatório apresenta o backend, os documentos e as execuções locais de Raniery, sem avaliar o andamento das outras frentes.

## 4. Evidências de teste

Raniery executou a aplicação e registrou os seguintes resultados da Sprint 1:

- PostgreSQL no Docker com estado `healthy`;
- 12 tabelas de domínio mais `flyway_schema_history`;
- 11 testes JUnit aprovados, sem falhas ou erros;
- 9 verificações HTTP aprovadas pelo script PowerShell;
- login administrativo com HTTP 200;
- acesso administrativo autorizado com HTTP 200;
- criação de usuário com HTTP 201 e confirmação na listagem;
- bloqueio de Leitor em rota administrativa com HTTP 403;
- bloqueio sem token e de senha incorreta com HTTP 401;
- atualização de usuário com HTTP 200 e nome alterado;
- inativação de usuário com HTTP 200 e `ativo: false`;
- acesso recusado com HTTP 401 usando o token anterior à inativação;
- cadastro de leitor com HTTP 201, IDs de leitor/usuário e resposta sem senha/hash;
- rejeição de documento duplicado com HTTP 409;
- criação do atendente usado na preparação com HTTP 201.

Os 17 arquivos de evidência foram organizados em uma tabela única no documento de testes.

Detalhamento completo: [sprint-1-evidencias-teste.md](sprint-1-evidencias-teste.md).

## 5. Retrospectiva e contribuição individual

- Registro de retrospectiva individual, elaborado por Raniery; não houve reunião coletiva: [sprint-1-retrospectiva.md](sprint-1-retrospectiva.md);
- Raniery: [sprint-1-contribuicao-raniery.md](sprint-1-contribuicao-raniery.md);

## 6. Riscos e impedimentos para a próxima sprint

| Risco/impedimento | Impacto | Ação definida/proposta |
|---|---|---|
| Integração com frontend não verificada nesta entrega | O aceite integrado de H1–H3 não pode ser confirmado por esta entrega de backend | Disponibilizar o contrato da API e validar os fluxos com a equipe. |
| Livros e exemplares não implementados nesta branch | Aumenta o volume da regra central da Sprint 2 | Raniery implementa H4/H5 junto com H6–H8 na branch da Sprint 2. |
| Integração entre devoluções e fila de reservas | H8 depende dos comportamentos de H9/H10 | Raniery e Vinicius alinham endpoints, estados e regras; equipe testa devolução com e sem fila. |

## 7. Uso de IA generativa

Houve apoio substancial de IA generativa na adaptação inicial do backend, nos testes, na revisão e na documentação. Raniery revisou o conteúdo, executou o sistema localmente e registrou as evidências apresentadas.
