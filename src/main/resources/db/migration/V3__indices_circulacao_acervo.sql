-- Complemento H6-H8: migrations V1/V2 não são alteradas.
CREATE INDEX ix_exemplar_livro_status ON exemplar(livro_id, status);
CREATE INDEX ix_emprestimo_leitor_ativo ON emprestimo(leitor_id) WHERE data_devolucao IS NULL;
CREATE INDEX ix_reserva_fila_livro ON reserva(livro_id, data_solicitacao, id) WHERE status = 'ativa';
CREATE INDEX ix_livro_autor_autor ON livro_autor(autor_id, livro_id);
CREATE INDEX ix_livro_categoria_categoria ON livro_categoria(categoria_id, livro_id);
