# Evidências de Teste — Sprint 4 — Biblioteca Viva

## 1. Casos de teste

| História | Caso de teste | Classe de teste |
|---|---|---|
| H12 | Reunir empréstimos, reservas e multas do leitor | `PainelLeitorServiceTest` |
| H12 | Calcular a posição da reserva na fila | `PainelLeitorServiceTest` |
| H12 | Não atribuir posição na fila a reservas inativas | `PainelLeitorServiceTest` |
| H12 | Retornar o painel com listas vazias | `PainelLeitorServiceTest` |
| H16 | Exportar o cabeçalho quando não existem movimentações | `RelatorioMovimentacaoServiceTest` |
| H16 | Exportar empréstimo em andamento | `RelatorioMovimentacaoServiceTest` |
| H16 | Exportar empréstimo devolvido | `RelatorioMovimentacaoServiceTest` |
| H16 | Formatar período com apenas a data inicial | `RelatorioMovimentacaoServiceTest` |
| H16 | Rejeitar período com data inicial posterior à data final | `RelatorioMovimentacaoServiceTest` |
| H16 | Escapar aspas no conteúdo do CSV | `RelatorioMovimentacaoServiceTest` |

## 2. Resultado da suíte automatizada

**Comando executado:** `.\mvnw.cmd clean verify`

**Resultado registrado:**
- Testes contabilizados: 150.
- Falhas: 0.
- Erros: 0.
- Testes ignorados: 47.
- Resultado final: `BUILD SUCCESS`.

Os testes ignorados pertencem às classes `CatalogoPostgresTest` (22 testes) e `CirculacaoPostgresTest` (25 testes).

## 3. Observações

Os casos de teste listados cobrem os principais comportamentos previstos para o painel do leitor e a exportação de movimentações.

A execução da suíte foi concluída sem falhas ou erros. Entretanto, os 47 testes ignorados não podem ser considerados aprovados, e sua validação permanece pendente.