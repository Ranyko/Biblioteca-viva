# Relatório Individual de Contribuição — Sprint 2 — Isaac (RA: 2840482421016)

**Papel nesta sprint:** Desenvolvedor Frontend e Integração com API

## 1. O que fiz
| Item | PR/commit | Status |
|---|---|---|
| Implementação da lógica de autenticação no `LoginForm.tsx` (`fetch` POST para `/auth/login`) |  | Concluído |
| Armazenamento seguro de Token JWT e dados de perfil do usuário no `localStorage` |  | Levado para a sprint 3 |
| Tratamento de estados visuais no formulário (mensagens de erro e estado de carregamento) |  | Concluído |
| Redirecionamento dinâmico do usuário pós-login utilizando React Router (`useNavigate`) | | Concluído |
| Resolução e alinhamento de comunicação e requisições HTTP entre Frontend e Spring Boot |  | Concluído |

## 2. Rituais que participei
- [] Dailies/weeklies
- [] Sprint Review
- [] Retrospectiva

## 3. PRs de colegas que revisei
Nenhuma revisão pull request registrada até o momento

## 4. Dificuldades e o que aprendi
Nesta Sprint 2, nosso foco principal foi realizar a integração real do Frontend React com o Backend em Spring Boot e PostgreSQL:

- **Comunicação e consumo de API:** Compreender e ajustar o envio correto do payload no corpo da requisição `POST` para corresponder exatamente ao esperado pelos DTOs do Spring Boot.
- **Gerenciamento de estado e persistência:** Garantir a persistência do Token JWT e dos dados do usuário no `localStorage` para manter a sessão ativa e controlar as mensagens de erro/carregamento diretamente na interface.
- **Tratamento de erros e CORS:** Lidar com cenários de falha na requisição e ajustar os cabeçalhos das requisições HTTP para permitir a comunicação fluida entre o ambiente local do Vite (`http://localhost:5173`) e a API (`http://localhost:8080`).
- **O que aprendi:** Aprofundei meus conhecimentos no consumo de APIs RESTful usando `async/await` com `fetch`, manipulação de respostas assíncronas em formulários React e controle de fluxo de navegação baseado no estado de autenticação.

## 5. Uso de IA

A maior dificuldade enfrentada nesta sprint foi, sem dúvidas, a integração entre o Frontend React e a API em Spring Boot no ambiente de desenvolvimento local. Durante os testes do formulário de login, as solicitações HTTP POST frequentemente resultavam no status 401 Unauthorized com acesso negado, o que exigiu um diagnóstico minucioso para identificar se o problema estava na formatação do payload JSON enviado pelo React ou na configuração de rotas e segurança do Spring Security no Backend. Somando-se a isso, instabilidades ao resetar o banco de dados PostgreSQL faziam com que a base voltasse zerada e sem tabelas, interrompendo o fluxo de autenticação e exigindo a reexecução das migrações para restabelecer a estrutura de dados.

Superar esses gargalos operacionais exigiu um aprofundamento prático na interpretação dos códigos de status e respostas da API REST. Compreender na prática a diferença entre falhas de autenticação (401), erros de rota ou requisição incorreta (400/404) e falhas internas do servidor (500) foi fundamental para diagnosticar a causa raiz dos problemas no terminal e no console do navegador, permitindo corrigi-los com agilidade no código e implementar um tratamento de mensagens de erro claro para o usuário final na interface.

Como principal aprendizado, essa experiência de integração ponta a ponta consolidou o entendimento sobre o funcionamento interno das APIs e a arquitetura cliente-servidor. Aprendi a lidar com chamadas assíncronas em JavaScript, a gerenciar a persistência do Token JWT e dos dados do usuário no localStorage, e a criar uma comunicação fluida e tolerante a falhas no Frontend, preparando a base da aplicação para os próximos módulos do sistema.