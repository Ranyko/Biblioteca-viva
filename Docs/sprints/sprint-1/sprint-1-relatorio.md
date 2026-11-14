# Relatório de Entrega — Sprint 1 — Biblioteca Viva

**Período:** 18/09/2026 a 02/10/2026
**Sprint Review:** não houve sprint review no presente momento

## 1. Planejado vs. entregue

| História (E2) | Planejada para esta sprint? | Entregue? | Observação |
|---|---|---|---|
| #1 Autenticação e Login | Sim | Parcial | Construção da interface visual de login no React/CSS a partir do Figma. Integração via API mantida para a Sprint 2. |
| #2 Gerenciamento de Usuários (Admin) | Sim | Parcial | Estrutura inicial das telas do Dashboard e navegação no Frontend. |
| #3 Cadastro de Leitores | Sim | Não | Replanejada para a Sprint 2. |
| #4 Cadastro de Livros, Autores e Categorias | Sim | Não | Replanejada para a Sprint 2. |
| #5 Cadastro e Controle de Exemplares | Sim | Não | Replanejada para a Sprint 2. |

---

## 2. Incremento funcional demonstrável
Construção das telas e componentes base da aplicação Web em React (TypeScript) a partir do layout e design do Figma:
* **Interface de Login:** Estrutura visual do formulário com estilização CSS.
* **Interface do Dashboard:** Layout da tela principal e prototipagem visual das seções.
* **Componentes Reutilizáveis:** Criação do componente `Navbar` para reaproveitamento entre as páginas do sistema.

* **Ambiente de execução:** Aplicação Frontend rodando localmente via Vite na porta `5173` (`npm run dev`).
* **Print/Demonstração:** `docs/sprints/sprint-1-demonstracao.png` `docs/sprints/sprint-1-demonstracao-login.png`
* **Passo a passo para reproduzir:** 
  1. Acesse a pasta `frontend/`.
  2. Execute `npm install` e em seguida `npm run dev`.
  3. Acesse `http://localhost:5173` no navegador para visualizar a interface das telas criadas.

---

## 3. Backlog atualizado
* **Board do Projeto:** `https://github.com/orgs/equipe-bibliotecaviva/projects/1`
* **Status ao fim da Sprint 1:**
  * **Histórias #1 e #2:** Em andamento no Frontend (telas criadas; conexão de endpoints e funcionalidades do Backend foram consolidadas e movidas para o escopo da **Sprint 2**).
  * **Histórias #3, #4 e #5:** Replanejadas para desenvolvimento no Backlog da **Sprint 2**.

---

## 4. Evidências de teste
* Validação visual e de layout das telas de Login e Dashboard em diferentes resoluções (responsividade).
* Testes de renderização dos componentes reutilizáveis (`Navbar`).
* Detalhes das evidências: `docs/sprints/sprint-1-evidencias-teste.md`.

---

## 5. Retrospectiva e contribuição individual
* **Ata de retrospectiva:** `docs/sprints/sprint-1-retrospectiva.md`
* **Relatórios individuais de contribuição:**
  * `docs/sprints/sprint-1-contribuicao-isaac.md`

---

## 6. Riscos/impedimentos para a próxima sprint
* **Foco da Sprint 1:** A Sprint 1 foi dedicada a "tirar o design do papel" e garantir que toda a estrutura visual em React/CSS estivesse pronta para uso.
* **Ação para a Sprint 2:** Conectar as telas criadas nos endpoints da API Spring Boot (login, JWT, envio de formulários e rotas protegidas).

---

## 7. Uso de IA Generativa
Houve utilização de ferramentas de Inteligência Artificial Generativa como apoio nas seguintes atividades da Sprint 1:
* **Construção e prototipagem inicial:** Auxílio na geração da estrutura base de componentes em React e estilização com CSS a partir do layout do Figma.
* **Revisão de código:** Utilizada para refatorar componentes visuais e aplicar boas práticas de reutilização no Frontend (como a extração do componente `Navbar`).
* **Documentação:** Apoio na estruturação e padronização dos relatórios Markdown da Sprint.