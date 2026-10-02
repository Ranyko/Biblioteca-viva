# Relatório Individual de Contribuição — Sprint 1 — Raniery Chiarelli (RA 2840482321007)

**Papel nesta sprint:** Product Owner e responsável pelo backend/dados

**Branch de trabalho:** [`feature/backend-sprint-1`](https://github.com/Ranyko/Biblioteca-viva/tree/feature/backend-sprint-1)

**Atualização do relatório:** 02/10/2026

## 1. O que fiz

| Item | PR/commit | Status |
|---|---|---|
| Adaptei a autenticação do Auth-Guard ao domínio, aos perfis e à tabela `usuario` do Biblioteca Viva | [`b5c9803`](https://github.com/Ranyko/Biblioteca-viva/commit/b5c9803) | Concluído na branch |
| Implementei JWT, BCrypt e autorização para Leitor, Atendente e Administrador | [`b5c9803`](https://github.com/Ranyko/Biblioteca-viva/commit/b5c9803) | Concluído e validado |
| Implementei listagem, criação e atualização de usuários e cadastro/listagem de leitores | [`b5c9803`](https://github.com/Ranyko/Biblioteca-viva/commit/b5c9803) | Implementado; atualização/inativação, bloqueio do token antigo, cadastro de leitor e documento duplicado demonstrados no Postman em 02/10 |
| Configurei PostgreSQL no Docker e migrations Flyway V1/V2 | [`b5c9803`](https://github.com/Ranyko/Biblioteca-viva/commit/b5c9803) | Concluído; banco saudável e 13 tabelas verificadas |
| Criei 11 testes unitários e o roteiro PowerShell de verificação da API | [`b5c9803`](https://github.com/Ranyko/Biblioteca-viva/commit/b5c9803) | 11 testes e 9 verificações HTTP aprovados |
| Atualizei o README com execução, arquitetura, usuários de demonstração e endpoints | [`b5c9803`](https://github.com/Ranyko/Biblioteca-viva/commit/b5c9803) | Concluído; ajuste final das evidências nesta branch |
| Removi arquivos de correção da disciplina que não deveriam permanecer no repositório | [`36264fb`](https://github.com/Ranyko/Biblioteca-viva/commit/36264fb) | Concluído |
| Executei os testes locais e organizei 17 arquivos de evidência da Sprint 1 | [`787a86c`](https://github.com/Ranyko/Biblioteca-viva/commit/787a86c51cc8a15ad10d055d840b2e6e02918ccb), referente às capturas iniciais | 11 testes unitários e 9 verificações do script aprovados; fluxos do Postman demonstrados |
| Organizei os documentos da Sprint 1 e consolidei o versionamento da entrega atual | [Branch do backend](https://github.com/Ranyko/Biblioteca-viva/tree/feature/backend-sprint-1) | Concluído |

Na organização informada para a Sprint 2, fico com o backend H4–H8; Vinicius fica com H9/H10 e Isaac com o frontend. Testes e revisão são responsabilidades da equipe. Este relatório individual registra apenas as implementações, execuções e documentos atribuídos a mim.


## 2. Rituais que participei

- [ ] Dailies/weeklies — Não houve nesta sprint.
- [ ] Sprint Review — Não houve nesta sprint.
- [ ] Retrospectiva coletiva — Não houve reunião nessa sprint.

Elaborei o [registro de retrospectiva individual](sprint-1-retrospectiva.md), refletindo sobre o que funcionou, as dificuldades e as melhorias propostas. O preenchimento desse documento foi uma atividade individual e não participação em uma reunião de retrospectiva.


## 3. PRs de colegas que revisei

Nenhuma revisão de pull request registrada até o momento.

## 4. Dificuldades e o que aprendi

A principal dificuldade foi adaptar um projeto de autenticação genérico para um schema acadêmico já definido, sem alterar as tabelas e regras construídas nas etapas anteriores. O Auth-Guard usava uma organização de permissões diferente, enquanto o Biblioteca Viva guarda o perfil diretamente em `usuario` e mantém os dados específicos do leitor em outra tabela.

Também aprofundei o uso do Flyway. A V1 representa o schema inicial e não deve ser alterada depois de aplicada, pois seu checksum fica registrado no banco. A V2 foi criada para corrigir o hash da senha de demonstração preservando o histórico da migration. Na segurança, passei a usar o ID imutável do usuário no JWT e a recarregar seu perfil e sua situação no banco em cada requisição, fazendo com que uma inativação tenha efeito mesmo sobre tokens já emitidos.

Na validação, pratiquei Maven Wrapper, JUnit/Mockito, Docker, PostgreSQL, Postman e interpretação dos códigos HTTP 200, 201, 401, 403 e 409. A execução também mostrou a importância de separar “código implementado” de “critério comprovado”.

## 5. Uso de IA generativa

Houve apoio substancial de IA generativa na adaptação inicial do backend, elaboração de testes, revisão técnica e documentação. Revisei o código, executei a aplicação e os testes localmente e registrei as evidências.
