# Extra Feature — Avaliações de Livros

## 1. Contexto

O catálogo permite consultar obras e disponibilidade, mas não registra a experiência dos leitores com os livros. Avaliações permitem compartilhar impressões e ajudam outros leitores a escolher o que ler.

Avaliação é individual, vinculada ao livro do acervo e não cria discussões entre participantes.

## 2. Objetivo

Permitir que leitores que já devolveram um livro atribuam uma nota de 1 a 5 e, opcionalmente, escrevam uma resenha curta. O acervo apresenta a média, a quantidade de avaliações e as resenhas mais recentes.

## 3. Escopo e regras de negócio

- Somente leitores autenticados podem criar, editar ou excluir a própria avaliação.
- Para avaliar uma obra, o leitor deve ter ao menos um empréstimo devolvido de um exemplar daquela obra.
- Cada leitor pode manter no máximo uma avaliação por livro. Uma restrição única no banco protege também contra criações concorrentes.
- A nota é obrigatória e deve estar entre 1 e 5. A resenha é opcional, limitada a 1000 caracteres e armazenada sem espaços excedentes nas extremidades.
- A avaliação é publicada imediatamente. Não há comentários, respostas, moderação nem identidade do leitor exposta na consulta pública.
- A consulta de avaliações é paginada, ordenada pelas alterações mais recentes e retorna nota média e total calculados sobre todas as avaliações do livro.
- Livros inexistentes ou inativos não podem receber avaliações e não exibem avaliações.
- A exclusão da avaliação remove-a da média e do total.

## 4. API

Todas as rotas exigem autenticação. As rotas `/leitor/**` exigem perfil leitor; a consulta sob `/acervo/**` pode ser feita por qualquer usuário autenticado.

| Método e rota | Descrição |
|---|---|
| `POST /leitor/livros/{livroId}/avaliacao` | Cria a avaliação do leitor autenticado. |
| `GET /leitor/livros/{livroId}/avaliacao` | Consulta a avaliação própria. |
| `PUT /leitor/livros/{livroId}/avaliacao` | Atualiza a nota e a resenha próprias. |
| `DELETE /leitor/livros/{livroId}/avaliacao` | Exclui a avaliação própria. |
| `GET /acervo/livros/{livroId}/avaliacoes?pagina=0&tamanho=20` | Lista avaliações e retorna média e total. |

O corpo de criação e atualização é `{"nota": 5, "resenha": "Leitura envolvente."}`. A identidade do leitor vem do token e nunca do corpo. Uma segunda criação ou a tentativa de avaliar sem empréstimo devolvido resulta em `409`; avaliação ou livro ausente resulta em `404`; nota, resenha ou paginação inválidas resultam em `400`.

## 5. Persistência

A migration `V8__avaliacoes_livros.sql` cria `avaliacao_livro`, com chaves para livro e leitor, unicidade por par, validação da nota e índice para a listagem recente. O schema manual e o fixture de integração acompanham a migration.

## 6. Critérios de aceite

- Um leitor com empréstimo devolvido pode criar e consultar a avaliação própria.
- Um leitor sem empréstimo devolvido da obra não pode avaliar; empréstimos ativos não contam.
- A API recusa notas fora de 1–5, resenhas acima de 1000 caracteres e paginação inválida.
- Uma segunda avaliação do mesmo leitor para o livro não é criada; outro leitor pode avaliar a mesma obra.
- Edição e exclusão afetam a média e o total consultados.
- As consultas não expõem o identificador nem os dados pessoais do leitor.
- Testes unitários verificam as regras; testes de integração exercitam PostgreSQL, autenticação, autorização e constraints da migration.
- É extremamente importante sanitizar o campo **resenha**, para que seja evitado **SQL Injection**.
