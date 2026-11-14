# Evidências de Teste — Sprint 2 — Biblioteca Viva

| ID | Caso de teste | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| CT01 | Autenticação de usuário com credenciais válidas e retorno de JWT | Integração / E2E | Passou | `docs/sprints/sprint-2/login-sucesso-200.png` |
| CT02 | Exibição de mensagem de erro ao informar credenciais inválidas | Interface / E2E | Passou | `docs/sprints/sprint-2/login-erro-401.png` |
| CT03 | Pesquisa de obras no acervo por título e filtro de disponibilidade (H6) | Interface / E2E | Passou | `docs/sprints/sprint-2/acervo-h6.png` |
| CT04 | Visualização dos detalhes da obra e quantidade de exemplares | Interface | Passou | `docs/sprints/sprint-2/detalhes-obra.png` |
| CT05 | Solicitação de reserva/empréstimo de exemplar elegível (H7) | Interface / Integração | Passou | `docs/sprints/sprint-2/evidencias-sprint-2/reserva-h7.png` |
| CT06 | Consulta da ordem e posição dos leitores na fila de reserva (H10) | Interface | Passou | `docs/sprints/sprint-2/evidencias-sprint-2/fila-reserva-h10.png` |




## Cobertura automatizada nesta sprint

A suíte automatizada do Backend (JUnit/Maven) reporta **111 testes executados com 100% de aprovação** (0 falhas e 0 erros). No Frontend, foram validados manualmente e via navegador os fluxos de renderização, navegação via React Router e integração HTTP/JWT para as telas H6, H7 e H10.