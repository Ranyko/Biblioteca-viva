# Termo de Aceite do Projeto — Biblioteca Viva

**Equipe:** Raniery Chiarelli (2840482321007) — Vinicius Rocha (2840482523051) — Isaac Leonardo da Silva (2840482421016)  
**Trilha:** B  
**Origem:** Banco de temas nº 9 — Controle de biblioteca comunitária  
**Data:** 28/08/2026

## 1. Escopo aceito para o semestre (funcionalidades Must + Should)

1. Autenticação e gerenciamento de usuários e leitores, com os perfis Leitor, Atendente e Administrador.
2. Cadastro de livros, autores, categorias e controle individual dos exemplares físicos.
3. Pesquisa do acervo por título, autor ou categoria, com indicação de disponibilidade.
4. Registro de empréstimos e devoluções, com validação do leitor, limite e disponibilidade do exemplar.
5. Reserva de obras indisponíveis, com fila por ordem de solicitação e prioridade para o primeiro leitor.
6. Cálculo e baixa de multas, com configuração de prazos, limites e valor diário.
7. Área do leitor para consulta dos próprios empréstimos, reservas e multas.
8. Dashboard com empréstimos ativos e atrasados, obras mais emprestadas e movimentações por categoria.
9. Sugestões de obras baseadas nas categorias presentes no histórico do leitor.

## 2. Critérios de pronto do MVP

- [ ] Os três perfis conseguem entrar no sistema e possuem permissões distintas.
- [ ] Administrador gerencia o acervo; atendente realiza o fluxo empréstimo → devolução → possível multa.
- [ ] Sistema impede empréstimos inválidos e mantém a fila de reservas na ordem correta.
- [ ] Leitor pesquisa o acervo e visualiza somente os próprios empréstimos, reservas e multas.
- [ ] Dashboard apresenta dados reais por meio de consulta agregada com três ou mais tabelas.
- [ ] Validações essenciais existem na interface e no banco, com os fluxos principais testados.
- [ ] Sistema está acessível por URL e o README permite executá-lo do zero.

## 3. Stack tecnológica definida

| Camada | Tecnologia |
|---|---|
| Frontend | React + Vite |
| Backend | Java + Spring Boot |
| Banco de dados | PostgreSQL |
| Autenticação | Spring Security + JWT |
| Versionamento | Git + GitHub |
| Deploy | Vercel para frontend e Render ou Railway para backend e banco |


## 4. Papéis iniciais da equipe (Sprint 1)

| Integrante | Papel |
|---|---|
| Raniery Chiarelli | Product Owner + Responsável por dados |
| Vinicius Rocha | Facilitador / Scrum Master |
| Isaac Leonardo da Silva | Responsável por qualidade |

## 5. Aprovação

- Professor: Lucas B. F. _______________________ Data: 28/08/2026
