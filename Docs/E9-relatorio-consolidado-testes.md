# Relatório de Testes Executados — Biblioteca Viva

**Data da bateria final:** 16/11/2026  
**Escopo testado:** Suíte automatizada do backend, abrangendo funcionalidades do MVP definidas no Termo de Aceite do Projeto (E2b), incluindo autenticação e autorização, gerenciamento de usuários, catálogo e consulta ao acervo, empréstimos, devoluções, reservas e fila de espera, multas, painéis e relatórios. Também foram contempladas funcionalidades complementares incorporadas durante o desenvolvimento, como avisos, clubes de leitura, avaliações e sugestões de livros.

## 1. Resumo executivo

| Métrica | Valor |
|---|---:|
| Total de testes executados | 277 |
| Aprovados | 277 |
| Falhas | 0 |
| Erros | 0 |
| Ignorados | 0 |
| Cobertura automatizada (linhas/branches) | Não medida |

A bateria final foi executada pelo GitHub Actions na branch `main`, no commit `00d03d5f0be91a7e06527d42a9565ddbbee5731e`, correspondente à integração do frontend realizada em 16/11/2026.

A execução utilizou o comando `./mvnw -B -ntp verify`, Java 21 e PostgreSQL 15. O resultado foi **BUILD SUCCESS**, com 277 testes executados, sem falhas, erros ou testes ignorados.

Os resultados foram consolidados a partir dos 29 relatórios individuais do Surefire. A cobertura percentual de linhas e branches não foi mensurada nesta bateria.

**Limitação:** os resultados apresentados correspondem à suíte automatizada do backend. Não representam uma execução automatizada de testes de interface ou de navegação completa pelo frontend.

## 2. Casos de teste — visão consolidada

| ID | Área | Tipo | Resultado | Observação |
|---|---|---|---|---|
| CT01 | Autenticação, tokens e segurança | Unitário | Passou (10 testes) | Verificação de autenticação, geração e validação de tokens e controle de acesso. |
| CT02 | Catálogo e consulta ao acervo | Unitário, integração e controller | Passou (49 testes) | Cadastro, consulta, validações, persistência e regras relacionadas a livros, autores, categorias e exemplares. |
| CT03 | Empréstimos, devoluções e circulação | Unitário, integração e controller | Passou (68 testes) | Regras de empréstimo, devolução, renovação, circulação e situações relacionadas. |
| CT04 | Reservas e fila de espera | Unitário e integração | Passou (9 testes) | Criação de reservas, prevenção de duplicidade, organização da fila e cancelamento. |
| CT05 | Multas e configurações | Integração | Passou (35 testes) | Regras de multas, baixas, configurações, autorização e concorrência. |
| CT06 | Avisos e notificações | Unitário e integração | Passou (30 testes) | Preferências, criação, leitura e geração automática de avisos. |
| CT07 | Clubes de leitura | Unitário, integração e controller | Passou (21 testes) | Gerenciamento de clubes, encontros, inscrições, capacidade e permissões. |
| CT08 | Avaliações de livros | Unitário e integração | Passou (13 testes) | Criação, consulta, alteração, exclusão e persistência de avaliações. |
| CT09 | Painéis e sugestões de livros | Unitário e repository | Passou (14 testes) | Consultas dos painéis administrativo e do leitor, além das regras de sugestões. |
| CT10 | Relatórios de movimentação | Unitário e integração | Passou (10 testes) | Consultas, períodos, geração e formatação de relatórios CSV. |
| CT11 | Repositórios complementares | Repository | Passou (18 testes) | Consultas e regras de persistência relacionadas aos painéis, sugestões e relatórios. |
| | **Total** | | **277 testes aprovados** | **0 falhas, 0 erros e 0 ignorados.** |

Os grupos acima organizam os testes por área funcional e técnica. O CT11 apresenta testes complementares de repositórios, dando suporte às funcionalidades de consulta e análise também descritas nos grupos CT09 e CT10.

## 3. Bugs encontrados nesta bateria

| # | Descrição | Severidade | Status |
|---|---|---|---|
| — | Nenhum bug foi registrado nos resultados da bateria final de testes automatizados. | — | Não foram registradas falhas ou erros. |

A ausência de falhas nesta execução não representa garantia de inexistência de defeitos no sistema. A conclusão se limita aos cenários cobertos pelos testes automatizados executados.

## 4. Testes de aceitação com cliente/PO

Não foram localizados registros documentais suficientes para confirmar a realização de uma bateria formal de testes exploratórios ou manuais com o cliente/PO na versão final do sistema.

O Termo de Aceite do Projeto (E2b) identifica o escopo inicialmente acordado, e a documentação do projeto identifica Raniery Chiarelli como PO. Entretanto, não há evidência disponível de uma sessão final de aceitação contendo data, participantes, roteiro executado, resultado e manifestação formal do responsável.

Dessa forma, os testes automatizados do backend foram concluídos com sucesso, mas a aceitação formal pelo cliente/PO permanece sem comprovação documental nesta entrega.

## 5. Conclusão

A bateria final de testes automatizados foi concluída com sucesso, totalizando 277 testes aprovados, sem falhas, erros ou testes ignorados.

Os resultados demonstram que os cenários automatizados contemplados na suíte foram executados conforme esperado. Entretanto, a cobertura percentual de código não foi mensurada e não há comprovação documental de uma sessão formal de aceitação pelo cliente/PO.

Assim, considera-se concluída a execução da bateria automatizada registrada neste relatório, mantendo-se essas limitações explicitadas para uma avaliação transparente dos resultados.