# Relatório Individual de Contribuição — Sprint 2 — Raniery Chiarelli

**RA:** 2840482321007

**Papel:** responsável pelo backend H4–H8 e dados correspondentes

**Branch de desenvolvimento:** `feature/backend-sprint-2`

**Atualização:** 02/10/2026

**Estado:** backend implementado e validado localmente, com resultados documentados.

## 1. Minha entrega

| Item da minha parte | O que acrescentei ao incremento | Resultado registrado |
|---|---|---|
| H4 | Autores, categorias e livros, múltiplos vínculos, validações e edição preservando IDs e movimentações | Testes automatizados e cadastros/validações no Postman aprovados |
| H5 | Exemplares com tombo único, conservação, disponibilidade inicial e situação protegida | Criação, duplicidade e bloqueio de inativação demonstrados; edição e demais regras cobertas pela suíte |
| H6 | Pesquisa por título, autor e categoria, paginação e contagem disponível | Consulta local demonstrada e testes PostgreSQL aprovados |
| H7 | Empréstimo com validação do leitor, limite, exemplar, prazo e funcionário autenticado | Empréstimo local demonstrado; regras e concorrência aprovadas na suíte |
| H8 | Devolução, atraso, multa e encaminhamento à fila elegível ou liberação | Devolução local e repetição demonstradas; atraso, multa e fila preparada cobertos pelos testes |
| Banco | Migration V3 de índices, transações e travas | 44 testes PostgreSQL aprovados; V1–V3 validadas pelo Flyway |
| Testes | Serviço/MVC, casos PostgreSQL, regressões da Sprint 1 e coleção Postman | 111 testes automatizados aprovados e 13 cenários HTTP com retorno esperado |
| Documentação | README, contrato da API, relatório, evidências, contribuição e retrospectiva | Atualizados com a execução local de 02/10/2026 |
| Versionamento | Trabalho em branch própria e configuração da verificação automática | Branch local `feature/backend-sprint-2` criada sobre o fechamento da Sprint 1 |

H4/H5 vieram do replanejamento da Sprint 1 e foram incluídas na minha parte junto com H6–H8. Este relatório descreve meu incremento de backend. As frentes H9/H10 e frontend mantêm seus próprios registros de contribuição.

## 2. Execuções que fiz

Em 02/10/2026, executei a suíte automatizada no Windows 11, usando Java 21.0.4 e PostgreSQL 15.19 em banco descartável. O resultado foi de **111 testes aprovados, sem falhas, erros ou casos ignorados**. Desse total, 44 exercitam a integração com PostgreSQL, incluindo quatro regressões da Sprint 1.

Também executei os 13 cenários da coleção Postman de H4–H8, do login administrativo ao cadastro de autor/categoria/livro/exemplar, pesquisa, empréstimo e devolução. Registrei as respostas esperadas para ISBN/tombo duplicados, ISBN ausente, tentativa de inativar exemplar emprestado e repetição da devolução.

As capturas e os resultados estão reunidos em [sprint-2-evidencias-teste.md](sprint-2-evidencias-teste.md).

## 3. Registro individual

A documentação da minha contribuição e o [registro de retrospectiva](sprint-2-retrospectiva.md) foram elaborados individualmente. A retrospectiva registra minha experiência com a implementação e a validação local.

## 4. Pontos técnicos e aprendizado

O incremento reutilizou schema, autenticação e perfis da Sprint 1. A separação entre livro e exemplar permitiu manter o cadastro da obra e o controle das unidades físicas, preservando as movimentações nas edições.

A execução com PostgreSQL complementou os testes isolados com verificações de SQL, Flyway, autorização, persistência, rollback e concorrência. O banco descartável permitiu repetir os casos com uma base conhecida. No Postman, o encadeamento dos IDs manteve a continuidade entre cadastro, empréstimo e devolução.

O contrato de travas e estados da circulação está documentado para a integração com H9/H10. Os resultados desta entrega se referem aos cenários executados do backend.

## 5. Uso de IA generativa

Utilizei apoio substancial do Codex na implementação, testes, revisão e documentação. A execução local dos testes, revisão do código/docs e a produção das capturas de 02/10/2026 foram realizadas por mim.
