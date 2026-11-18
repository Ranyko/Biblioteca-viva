# Documentação Final — Biblioteca Viva

**Deploy definitivo:** https://biblioteca-viva.duckdns.org/  
**Repositório:** https://github.com/Ranyko/Biblioteca-viva

## 1. Índice de artefatos do semestre

| Entrega | Documento | Link |
|---|---|---|
| E1 | Documento de Visão | [Documento de Visão — E1](https://github.com/Ranyko/Biblioteca-viva/blob/main/Docs/E1-E4/documento-de-visao-biblioteca-viva%20(E1).md) |
| E2 | Backlog + Termo de Aceite do Projeto | [Backlog E2](https://github.com/Ranyko/Biblioteca-viva/blob/main/Docs/E1-E4/backlog-biblioteca-viva(E2).md) · [Termo de Aceite](https://github.com/Ranyko/Biblioteca-viva/blob/main/Docs/E1-E4/termo-de-aceite-biblioteca-viva(E2).md) |
| E3 | UML + DER + DDL | [Diagrama UML](https://github.com/Ranyko/Biblioteca-viva/blob/main/Docs/E1-E4/UML.md) · [DER](https://github.com/Ranyko/Biblioteca-viva/blob/main/Docs/E1-E4/der.md) · [Schema SQL](https://github.com/Ranyko/Biblioteca-viva/tree/main/src/main/resources/db/migration) |
| E4 | README + Protótipo + Plano de Testes | [README](https://github.com/Ranyko/Biblioteca-viva/blob/main/README.md) · [Roteiro do Protótipo](https://github.com/Ranyko/Biblioteca-viva/blob/main/Docs/E1-E4/Roteiro%20do%20Prot%C3%B3tipo%20Naveg%C3%A1vel.md) · [Plano de Testes](https://github.com/Ranyko/Biblioteca-viva/blob/main/Docs/E1-E4/plano-de-testes.md) |
| E5–E8 | Relatórios de sprint e evidências | [Documentação das entregas e sprints](https://github.com/Ranyko/Biblioteca-viva/tree/main/Docs) |
| E9 | Relatório de Testes Executados + Termo de Aceite do Cliente | Documentos preparados para inclusão no repositório. |
| E10 | Documentação Final + Manual do Usuário | Documentos desta etapa. |

## 2. Desvios do escopo original (vs. E2)

Durante o desenvolvimento, foram implementadas funcionalidades adicionais além das histórias originalmente previstas no backlog E2. Essas funcionalidades foram incorporadas ao produto, mas não alteram retroativamente o escopo inicial do projeto.

| Item | Mudou? | Justificativa |
|---|---|---|
| H1–H16 — Histórias do backlog | Não | Mantidas como referência do escopo original. Algumas histórias foram replanejadas entre sprints, mas permaneceram previstas no backlog. |
| Central de avisos internos | Sim | Funcionalidade adicional para ampliar a comunicação entre o sistema e seus usuários. A implementação contempla avisos internos, sem integração de envio por e-mail ou WhatsApp. |
| Clubes de leitura | Sim | Funcionalidade adicional para permitir a criação e o gerenciamento de clubes, encontros e inscrições. |
| Renovação de empréstimos | Sim | Funcionalidade adicional que permite renovar empréstimos e consultar o histórico de renovações. |
| Avaliações de livros | Sim | Funcionalidade adicional para registrar, consultar e gerenciar avaliações das obras pelos leitores. |
| Interface para clubes e renovação | Sim | Ampliação da interface para disponibilizar as funcionalidades adicionais incorporadas ao sistema. |
| Correções do painel administrativo | Não | Ajustes realizados para restaurar e manter o comportamento esperado da funcionalidade prevista no backlog. |
| H17 — Avisos por e-mail ou WhatsApp | Não implementada | Permaneceu fora do escopo, conforme classificação Won't do backlog E2. |
| H18 — Pagamento de multas pela internet | Não implementada | Permaneceu fora do escopo, conforme classificação Won't do backlog E2. |

As funcionalidades adicionais foram desenvolvidas e integradas à versão final do sistema. Elas representam ampliações voluntárias do produto, e não alterações dos requisitos originalmente estabelecidos.

## 3. Arquitetura final (resumo)

A versão final do Biblioteca Viva utiliza uma arquitetura cliente-servidor, organizada em frontend, backend e banco de dados relacional.

**Frontend**
- Desenvolvido com React e TypeScript.
- Responsável pela interface de interação dos perfis Administrador, Atendente e Leitor.
- Consome os endpoints disponibilizados pelo backend para autenticação, consultas e operações do sistema.

**Backend**
- Desenvolvido em Java 21 com Spring Boot 4.
- Organizado em camadas de controllers, services, repositories, entidades e componentes de configuração.
- Spring Security e JWT são utilizados para autenticação e autorização.
- As regras de negócio são aplicadas na camada de serviços, enquanto os controllers disponibilizam os endpoints da API.

**Banco de dados**
- PostgreSQL 15.
- Spring Data JPA para persistência e consultas.
- Flyway para versionamento e aplicação das alterações estruturais do banco.

**Infraestrutura e implantação**
- Docker Compose para execução do ambiente de banco de dados.
- Aplicação disponibilizada publicamente por meio do endereço definitivo do projeto.
- Repositório GitHub utilizado para versionamento, organização do código e documentação.

O fluxo geral de funcionamento é:

`Usuário → Frontend React → API Spring Boot → PostgreSQL`

O frontend envia as requisições à API, que valida a autenticação e as permissões do usuário, executa as regras de negócio e realiza as operações necessárias no banco de dados.

A arquitetura final ampliou a proposta inicial de uma API para contemplar também a interface web integrada, permitindo a utilização do sistema pelos diferentes perfis de usuário.

## 4. Como contribuir/rodar

O código-fonte, as tecnologias utilizadas, os pré-requisitos, as instruções de configuração, os comandos de execução e os procedimentos de teste estão descritos no [README principal do repositório](https://github.com/Ranyko/Biblioteca-viva/blob/main/README.md).

O README apresenta as orientações para configuração do ambiente de desenvolvimento, execução do PostgreSQL, inicialização do backend, autenticação, endpoints disponíveis e execução dos testes automatizados.

Para consultar a versão implantada e acessar o sistema, utilize:

**Deploy:** https://biblioteca-viva.duckdns.org/

Para contribuir com o projeto, recomenda-se utilizar branches específicas para cada alteração, manter os commits organizados e submeter as modificações por Pull Requests, preservando a rastreabilidade do desenvolvimento. 