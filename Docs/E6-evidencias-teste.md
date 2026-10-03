# Evidências de Teste — Sprint 2 — Biblioteca Viva

| ID | Caso de teste | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| CT09 | Criar reserva para obra sem exemplares disponíveis | Manual | Não executado | Ambiente backend indisponível por falha de conexão com banco Neon |
| CT10 | Impedir reserva duplicada da mesma obra pelo mesmo leitor | Manual | Não executado | Ambiente backend indisponível por falha de conexão com banco Neon |
| CT11 | Exibir posição correta na fila de reservas | Manual | Não executado | Ambiente backend indisponível por falha de conexão com banco Neon |
| CT12 | Consultar fila de reservas ordenada por data da solicitação | Manual | Não executado | Ambiente backend indisponível por falha de conexão com banco Neon |

## Cobertura automatizada nesta sprint

Não foram executados testes automatizados nesta sprint.

Motivo:
Durante a implementação das histórias H9 e H10 foi identificada falha de conexão entre a aplicação e o banco de dados Neon, impedindo a inicialização completa da API e, consequentemente, a execução dos cenários planejados.

Os testes permanecem registrados no plano de testes para execução na próxima sprint após normalização do ambiente.