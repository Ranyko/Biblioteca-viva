# Relatório de Entrega — Sprint 3 — Biblioteca Viva

**Período:** Não informado  
**Sprint Review:** Data e participantes não informados.

## 1. Planejado vs. entregue

| História | Planejada para esta sprint? | Situação | Observação |
|---|---|---|---|
| H11 — Calcular multa por atraso | Sim | Revisão pendente | Foi realizada uma observação informal da implementação, sem code review formal. |
| H12 — Acompanhar empréstimos, reservas e multas | Sim | Replanejada | Será implementada na Sprint 4. |
| H13 — Configurar regras da biblioteca | Sim | Revisão pendente | Foi realizada uma observação informal da implementação, sem code review formal. |
| H14 — Consultar painel da biblioteca | Sim | Entregue | Implementação publicada na branch de trabalho. |
| H15 — Receber sugestões de livros | Sim | Entregue | Implementação publicada na branch de trabalho e suíte automatizada aprovada. |

## 2. Incremento funcional demonstrável

Foram implementadas as funcionalidades de consulta do painel administrativo da biblioteca (H14) e de sugestões de livros baseadas no histórico de empréstimos do leitor (H15).

A H15 utiliza uma consulta ao banco de dados para considerar as categorias presentes no histórico, excluir obras já emprestadas pelo leitor e priorizar livros com exemplares disponíveis.

A execução local de `.\mvnw.cmd clean test` foi concluída com sucesso, com 33 testes executados, sem falhas ou erros.

## 3. Backlog atualizado

O backlog do projeto mantém a H12 como história originalmente planejada para a Sprint 3. Entretanto, devido ao replanejamento da equipe, sua implementação será transferida para a Sprint 4.

A H16 — Exportar relatório de movimentações em CSV — já está prevista no backlog para a Sprint 4.

## 4. Evidências de teste

Os testes automatizados executados localmente foram aprovados. O detalhamento dos cenários e resultados está registrado em `E7-evidencias-teste.md`.

## 5. Retrospectiva e contribuição individual

A retrospectiva desta sprint destaca dificuldades no processo de merge e a percepção de pouco respaldo e auxílio do professor.

O relatório individual registra as implementações H14 e H15, os testes realizados e a observação informal das implementações de H11 e H13, sem caracterizá-la como revisão formal de código.

## 6. Riscos e impedimentos para a próxima sprint

- A H12 permanece pendente e deverá ser planejada para a Sprint 4.
- As dificuldades no processo de merge precisam ser consideradas no planejamento das próximas alterações.
- A configuração do banco de dados ainda precisa ser revisada posteriormente, conforme já previsto no histórico do projeto.