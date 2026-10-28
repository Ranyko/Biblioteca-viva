# Evidências de Teste — Sprint 3 — Biblioteca Viva

## 1. Casos de teste

| ID | Caso de teste | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| CT14 | Consultar dados do painel administrativo | Automatizado | A suíte foi aprovada | Execução local de `.\mvnw.cmd clean test` |
| CT15 | Impedir acesso ao endpoint de sugestões sem autenticação | Automatizado | Passou | `LeitorControllerTest` |
| CT16 | Permitir consulta de sugestões por leitor autenticado | Automatizado | Passou | `LeitorControllerTest` |
| CT17 | Impedir acesso de atendente ao endpoint de sugestões de leitor | Automatizado | Passou | `LeitorControllerTest` |
| CT18 | Não recomendar obras já emprestadas pelo leitor | Automatizado | Passou | `SugestaoLivroRepositoryIntegracaoTest` |
| CT19 | Priorizar livros com exemplares disponíveis | Automatizado | Passou | `SugestaoLivroRepositoryIntegracaoTest` |
| CT20 | Não retornar livros duplicados nas sugestões | Automatizado | Passou | `SugestaoLivroRepositoryIntegracaoTest` |
| CT21 | Priorizar a categoria mais frequente no histórico | Automatizado | Passou | `SugestaoLivroRepositoryIntegracaoTest` |

## 2. Resultado da suíte automatizada

**Comando executado:** `.\mvnw.cmd clean test`

**Resultado registrado:**
- Testes executados: 33.
- Falhas: 0.
- Erros: 0.
- Testes ignorados: 0.
- Resultado final: `BUILD SUCCESS`.

A execução foi realizada localmente. Este registro não afirma que a mesma execução tenha ocorrido em integração contínua (CI).

## 3. Observações

Os testes da H15 verificam os critérios de recomendação e o controle de acesso ao endpoint de sugestões. A suíte completa também inclui testes de outras funcionalidades do projeto.

As evidências descritas correspondem aos resultados registrados na execução dos testes. Capturas de tela adicionais não foram identificadas como parte desta entrega.