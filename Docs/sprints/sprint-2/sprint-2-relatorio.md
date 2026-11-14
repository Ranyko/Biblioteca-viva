# Relatório de Entrega — Sprint 2 — Biblioteca Viva

**Equipe:** Raniery Chiarelli (RA 2840482321007) — Vinicius Rocha (RA 2840482523051) — Isaac Leonardo da Silva (RA 2840482421016)

**Atualização:** 02/10/2026

**Responsável pelo incremento documentado:** Isaac Leonardo — Frontend e Integração com API

**Branch:** `feature/frontend-sprint-2`

**Estado:** Frontend das telas H6, H7, H8 e H10 implementado e integrado aos endpoints do Backend, com fluxo de autenticação via Token JWT validado no ambiente local em 02/10/2026.

---

## 1. Planejado vs. entregue na minha parte

| História / Tela | Origem | Implementação disponível | Validação |
|---|---|---|---|
| **Autenticação (Login & JWT)** | Conclusão da Sprint 1 | Formulário em `LoginForm.tsx` integrado com a API (`/auth/login`), gerenciamento de Token JWT e perfil de usuário salvos no `localStorage`, com tratamento visual de erros (401) e carregamento | Testado e aprovado localmente no navegador (Vite) via requisição POST `200 OK` e `401 Unauthorized` |
| **H6 — Pesquisa do acervo & Detalhes da Obra** | Sprint 2 | Interface da página de Acervo com listagem de obras, filtros por título, autor/categoria e indicador visual de quantidade de exemplares disponíveis; Modal/Página de Detalhes da Obra | Renderização e navegação validadas no React; integração com a consulta de exemplares |
| **H7 — Empréstimo e Solicitação de Reserva** | Sprint 2 | Interface para registrar solicitação de empréstimo/reserva de exemplares para leitores elegíveis | Formulários e ações de solicitação configurados com validação visual de disponibilidade |
| **H10 — Consultar fila de reserva** | Sprint 2 | Tela/Aba visual para consulta da ordem e posição da fila de espera por obra | Renderização da lista de leitores e posições da fila estruturada no React |
| **H8 — Registrar devolução** | Sprint 2 | Interface operacional para o atendente registrar a devolução de exemplares, com feedback de prazos e atrasos | Interface integrada com o fluxo de devolução do acervo |

---

## 2. Incremento funcional

A entrega desta Sprint 2 consolida a conexão entre a interface desenvolvida em React e os endpoints REST em Spring Boot com banco de dados PostgreSQL. O foco do incremento esteve em integrar o fluxo de autenticação real e disponibilizar as visões do sistema para circulação e consulta do acervo:

- **Autenticação & Sessão:** Login dinâmico com salvamento automático do Token JWT no `localStorage`, permitindo identificar o perfil e liberar as funcionalidades permitidas.
- **Acervo e Detalhes da Obra (H6):** Visualização completa do catálogo de livros com detalhes sobre autores, categorias e disponibilidade de exemplares físicos.
- **Reservas e Empréstimos (H7 e H10):** Prototipagem funcional e telas para acompanhamento das solicitações de empréstimo e ordem da fila de espera.
- **Operação de Devolução (H8):** Tela própria para lançamento da devolução de exemplares.

---

## 3. Responsabilidades

| Frente informada | Responsável | Relação com este incremento |
|---|---|---|
| Frontend e consumo das APIs (H6, H7, H8, H10 e Login) | Isaac | Implementação dos componentes React, páginas, consumo via `fetch`/JWT e tratamento de erros na interface |
| Backend H4–H8 e banco de dados | Raniery | Disponibilização dos endpoints REST de login, acervo, exemplares, empréstimos e devoluções |
| Backend H9/H10 (reservas e fila) | Vinicius | Regras de negócio e rotas das filas de reserva para consumo do frontend |
| Testes e revisão | Toda a equipe | Validação e conferência integrada da comunicação entre React e Spring Boot |

---

## 4. Resultados da execução local

| Verificação em 02/10/2026 | Resultado |
|---|---|
| **Ambiente Frontend (Vite/React)** | Executado com sucesso na porta `http://localhost:5173` sem erros de compilação ou rotas |
| **Autenticação (Login POST)** | Respostas `200 OK` validadas com gravação de JWT no `localStorage`; tratamento de credenciais inválidas exibindo erro `401 Unauthorized` |
| **Comunicação Cliente-Servidor** | Integração validada via servidor local do Spring Boot (`http://localhost:8080`) e PostgreSQL |
| **Responsividade e Navegação** | Rotas protegidas e navegação via React Router testadas nas abas de Acervo, Detalhes, Reservas e Devoluções |

---

## 5. Alcance da entrega e integração

As telas e fluxos desenvolvidos cobrem a experiência visual e a integração dos principais módulos de circulação da biblioteca planejados para a Sprint 2. Ajustes de ambiente durante a sprint — tais como a resolução de inconsistências no banco de dados e o alinhamento das respostas HTTP 401 — garantiram que a camada do cliente lide adequadamente com situações de falha e retornos da API.

---

## 6. Documentos individuais

- [Contribuição individual](sprint-2-contribuicao-isaac.md)

---

## 7. Uso de IA generativa

Houve apoio de IA generativa na estruturação das requisições assíncronas do Frontend, auxílio na resolução e diagnóstico de erros de comunicação HTTP/CORS e na revisão da documentação desta sprint. Isaac realizou o desenvolvimento das páginas, a execução e validação no ambiente local e o registro das evidências apresentadas.