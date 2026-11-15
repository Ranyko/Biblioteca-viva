# Evidências de Teste — Sprint 3 — Backend H11 e H13

**Responsável pelo incremento:** Raniery Chiarelli — RA 2840482321007

**Data da execução automatizada:** 16/10/2026

**Executor dos testes e responsável pela revisão:** Raniery Chiarelli

**Ambiente:** execução local com Maven Wrapper e PostgreSQL em banco descartável.

## 1. Resultado consolidado

`clean verify`: **149 testes executados e aprovados, zero falhas, zero erros e zero ignorados**.

A suíte automatizada foi executada localmente por Raniery Chiarelli, que também revisou o código, os documentos e os resultados registrados.

| Grupo | Quantidade aprovada |
|---|---:|
| Serviço, segurança e MVC existentes | 67 |
| Integração PostgreSQL H1–H8 existente | 47 |
| Integração PostgreSQL H11/H13 adicionada | 35 |
| Total | 149 |

A classe [MultasConfiguracaoPostgresTest](../../../src/test/java/br/com/bibliotecaviva/integration/MultasConfiguracaoPostgresTest.java) usa Spring Boot, MockMvc, JWT, Flyway e PostgreSQL reais. A fixture é aplicada somente em banco cujo nome termina em `_sprint2_test`, conforme a proteção da suíte existente.

## 2. Cenários H11/H13 aprovados

| Grupo | Comportamento verificado |
|---|---|
| Autenticação e perfis | HTTP 401 sem token e após inativação do usuário; HTTP 403 para leitor nas rotas operacionais e para atendente na configuração; administrador autorizado a consultar e baixar multa. |
| Consulta de multas | Lista vazia, resposta paginada, valores históricos, vínculo com empréstimo/leitor/exemplar, filtros combinados e ordem estável por ID. |
| Validação de consultas | IDs, situação e paginação inválidos retornam HTTP 400; multa ausente retorna HTTP 404. |
| Baixa | Multa pendente passa a quitada, com data do servidor e valor preservado; situação cancelada e segunda baixa retornam HTTP 409. |
| Concorrência de baixa | Duas solicitações simultâneas resultam em um HTTP 200 e um HTTP 409, com um único pagamento registrado. |
| Configuração | Consulta inicial e atualização completa dos cinco parâmetros, com data definida pelo servidor. |
| Dados inválidos | Dez variações de corpo inválido, incluindo ausência de campos, valores não positivos, excesso de precisão e estouro dos limites do schema, retornam HTTP 400 e preservam as regras anteriores. JSON malformado também é recusado. |
| Configuração ausente | Consulta e atualização retornam HTTP 404 e não criam outra linha. |
| Efeito na circulação | Novo prazo aplicado ao empréstimo posterior; limite reduzido respeitado; data prevista do empréstimo anterior preservada. |
| Valores históricos | Alterar diária/teto não recalcula multa já gerada; a nova devolução usa diária e teto atualizados. Diária zero não gera cobrança. |
| Reserva preparada | A devolução aplica o novo prazo de retirada à reserva preparada no banco. |
| Concorrência de configuração | Atualizações simultâneas mantêm um conjunto completo de regras, sem misturar campos de duas gravações. |

## 3. Reprodução e coleção HTTP

Os comandos e a criação do banco descartável estão no [README](../../../README.md#testes-de-integração-com-postgresql). A variável `SPRINT2_TEST_DB_URL` habilita também os casos H11/H13.

A [coleção Postman H11/H13](../../Postman/sprint-3-h11-h13.postman_collection.json) contém 13 requisições de autenticação, consulta, alteração, validação e baixa. A baixa exige uma multa pendente e altera seu registro; a última requisição restaura as regras consultadas no início.

Os resultados deste documento correspondem à execução da suíte automatizada.
