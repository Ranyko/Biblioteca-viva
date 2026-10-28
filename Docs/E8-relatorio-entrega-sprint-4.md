# Relatório de Entrega — Sprint 4 — Biblioteca Viva

**Período:** Não informado  
**Sprint Review:** Data e participantes não informados.

## 1. Planejado vs. entregue

| História | Planejada para esta sprint? | Situação | Observação |
|---|---|---|---|
| H12 — Acompanhar empréstimos, reservas e multas | Sim | Implementada; revisão final pendente | O painel reúne empréstimos, reservas, posição na fila e multas do leitor. |
| H16 — Exportar relatório de movimentações em CSV | Sim | Implementada; revisão final pendente | A exportação permite filtrar movimentações por período e gerar arquivo CSV. |

## 2. Incremento funcional demonstrável

A H12 disponibiliza um painel para consulta de empréstimos, reservas e multas. Para reservas ativas, o sistema calcula a posição do leitor na fila.

A H16 permite exportar movimentações da biblioteca em CSV, com informações sobre obras, leitores, retiradas, prazos, devoluções e situação dos empréstimos.

## 3. Validação técnica

A execução local de `.\mvnw.cmd clean verify` foi concluída com `BUILD SUCCESS`, contabilizando 150 testes, sem falhas ou erros, e 47 testes ignorados.

Os testes ignorados permanecem pendentes de validação.

## 4. Integração e próximos passos

As alterações foram reunidas na branch `integracao/minhas-funcionalidades`, publicada no GitHub.

A revisão final das alterações e a integração à branch `main` permanecem pendentes.