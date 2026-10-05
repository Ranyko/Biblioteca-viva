-- Usado exclusivamente por CirculacaoPostgresTest em banco cujo nome termina em _sprint2_test.
TRUNCATE multa, reserva, emprestimo, exemplar, livro_autor, livro_categoria,
    livro, autor, categoria, leitor, usuario, configuracao_biblioteca RESTART IDENTITY CASCADE;
INSERT INTO configuracao_biblioteca VALUES (1, 14, 3, 2.50, 25.00, 2, CURRENT_TIMESTAMP);
INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES
('Administrador', 'admin@biblioteca.com', '$2b$12$Fag2Czl58TQJ.hC9v5qoJuzqNtz9c8x6j8JPIlUbOdXCM4b.UKIyy', 'administrador'),
('João', 'joao@biblioteca.com', '$2b$12$Fag2Czl58TQJ.hC9v5qoJuzqNtz9c8x6j8JPIlUbOdXCM4b.UKIyy', 'leitor'),
('Maria', 'maria@biblioteca.com', '$2b$12$Fag2Czl58TQJ.hC9v5qoJuzqNtz9c8x6j8JPIlUbOdXCM4b.UKIyy', 'leitor'),
('Carlos', 'carlos@biblioteca.com', '$2b$12$Fag2Czl58TQJ.hC9v5qoJuzqNtz9c8x6j8JPIlUbOdXCM4b.UKIyy', 'atendente');
INSERT INTO leitor(usuario_id, documento) VALUES (2, '111'), (3, '222');
INSERT INTO autor(nome) VALUES ('Machado de Assis'), ('Clarice Lispector');
INSERT INTO categoria(nome) VALUES ('Romance'), ('Literatura Brasileira');
INSERT INTO livro(titulo, isbn, ano_publicacao) VALUES
('Dom Casmurro', '1234567890123', 1899), ('A Hora da Estrela', '9876543210123', 1977),
('Sem Exemplares', '1111111111111', 2000);
INSERT INTO livro_autor VALUES (1, 1), (2, 2);
INSERT INTO livro_categoria VALUES (1, 1), (1, 2), (2, 2);
INSERT INTO exemplar(livro_id, codigo_tombo, estado_conservacao, status) VALUES
(1, 'T1', 'bom', 'disponivel'), (1, 'T2', 'bom', 'emprestado'),
(2, 'T3', 'bom', 'disponivel'), (2, 'T4', 'danificado', 'inativo');
INSERT INTO emprestimo(leitor_id, exemplar_id, atendente_retirada_id, data_retirada, data_prevista_devolucao)
VALUES (1, 2, 4, '2026-09-10T10:00:00-03:00', '2026-09-24');
