# Relatório de Entrega — Sprint 2 — Biblioteca Viva

**Equipe:** Raniery Chiarelli (RA 2840482321007) — Vinicius Rocha (RA 2840482523051) — Isaac Leonardo da Silva (RA 2840482421016)

**Atualização:** 02/10/2026

**Responsável pelo incremento documentado:** Raniery Chiarelli — backend e dados

**Branch:** `feature/backend-sprint-2`

**Estado:** backend H4–H8 implementado e validado localmente em 02/10/2026, com 111 testes automatizados aprovados e 13 cenários Postman com os retornos esperados.

## 1. Planejado vs. entregue na minha parte

| História | Origem | Implementação disponível | Validação |
|---|---|---|---|
| H4 — Livros, autores e categorias | Replanejada da Sprint 1 | Cadastro, consulta e edição; múltiplos vínculos; ISBN obrigatório, normalizado e único | Serviço/MVC e PostgreSQL aprovados; cadastros e bloqueios demonstrados no Postman |
| H5 — Exemplares | Replanejada da Sprint 1 | Tombo único, vínculo com livro, conservação, disponibilidade inicial e proteção das movimentações | Serviço/MVC e PostgreSQL aprovados; cadastros e bloqueios demonstrados no Postman |
| H6 — Pesquisa do acervo | Sprint 2 | Filtros por título, autor e categoria; paginação; quantidade disponível; obras sem disponibilidade continuam visíveis | Filtros, paginação e disponibilidade aprovados na suíte; pesquisa local demonstrada |
| H7 — Empréstimos | Sprint 2 | Leitor ativo, limite, exemplar elegível, prazo automático, funcionário autenticado e transação | Regras e concorrência aprovadas na suíte; empréstimo local com prazo automático demonstrado |
| H8 — Devoluções | Sprint 2 | Data, atraso, multa com teto, liberação ou alocação à fila elegível e rejeição de repetição | Serviço/MVC e PostgreSQL aprovados; devolução e repetição demonstradas; fila testada com fixture |

O replanejamento de H4/H5 está registrado nesta sprint. Os documentos E1–E4 mantêm seu conteúdo anterior. A edição conserva IDs e movimentações relacionadas; não há versionamento temporal dos metadados de livros.

## 2. Incremento funcional

O backend da Sprint 1 foi preservado. Minha entrega acrescenta o cadastro e a consulta do acervo, o controle de exemplares e o fluxo de empréstimo/devolução, usando a autenticação e os perfis existentes.

- Administrador: autores, categorias, livros e exemplares.
- Usuário autenticado: pesquisa do acervo.
- Atendente ou administrador: empréstimos, consultas operacionais e devoluções.
- Flyway V3: índices de apoio, mantendo V1/V2 intactas.
- Clock configurável: datas calculadas pelo servidor no fuso `America/Sao_Paulo`.
- Testes unitários/MVC, casos PostgreSQL, coleção Postman e workflow de verificação.

O [README](../../../README.md#contrato-do-backend-h4-a-h8) apresenta endpoints, corpos e regras. A [coleção Postman](../../Postman/sprint-2-h4-h8.postman_collection.json) demonstra o fluxo cadastrar → pesquisar → emprestar → devolver.

## 3. Responsabilidades

| Frente informada | Responsável | Relação com este incremento |
|---|---|---|
| Backend H4–H8 e seus documentos | Raniery | Implementação e documentação apresentadas nesta entrega |
| H9/H10 — reservas e fila | Vinicius | Alinhar estados e contrato usados pela devolução |
| Frontend e consumo da API | Isaac | Conferir contrato e validar fluxos integrados |
| Testes e revisão | Toda a equipe | Validar o próprio incremento e participar da conferência integrada |

Este documento registra a contribuição de backend de Raniery. As demais frentes mantêm seus próprios registros; os resultados aqui apresentados se referem ao incremento H4–H8.

## 4. Resultados da execução local

| Verificação em 02/10/2026 | Resultado |
|---|---|
| Maven/JUnit no computador de Raniery | 111 testes aprovados; 0 falhas, 0 erros e 0 ignorados |
| Serviço, segurança e MVC | 67 testes aprovados, incluindo os 11 originais da Sprint 1 |
| PostgreSQL | 44 testes aprovados; quatro são regressões dos fluxos da Sprint 1 |
| Flyway | V1, V2 e V3 validadas e aplicadas no banco descartável |
| Postman | 13 cenários com os retornos esperados, do login à devolução e seus bloqueios |

Ambiente: Windows 11, Java 21.0.4 e PostgreSQL 15.19. Os relatórios Surefire registram os totais por classe e a execução com o banco `biblioteca_sprint2_test`. As capturas demonstram o fluxo com livro 5, exemplar 7 e empréstimo 3, prazo de 14 dias e devolução no mesmo dia sem multa.

Detalhamento: [sprint-2-evidencias-teste.md](sprint-2-evidencias-teste.md).

## 5. Alcance da entrega e integração

Os cenários executados confirmam as regras de cadastro, pesquisa, circulação, autorização, persistência e concorrência cobertas pela suíte e pela demonstração local. A fila exercitada nesta suíte usa dados preparados por fixture. A integração completa com a API H9/H10 e com as telas está fora do alcance dos resultados aqui registrados.

O contrato de travas, estados e prazo de retirada está descrito no README para o alinhamento entre circulação e reservas. A retirada de reserva própria está implementada conforme o cenário previsto no plano de testes, complementando o critério de disponibilidade de H7.

A geração de multa na devolução compõe H8. Consulta e baixa de multas da H11 pertencem a outro incremento.

## 6. Documentos individuais

- [Contribuição individual](sprint-2-contribuicao-raniery.md)
- [Retrospectiva individual](sprint-2-retrospectiva.md)

## 7. Uso de IA generativa

Houve apoio substancial do Codex no código, testes, revisão e documentos. Raniery realizou a execução local dos testes, revisão do código/docs e a produção das capturas de 02/10/2026.
