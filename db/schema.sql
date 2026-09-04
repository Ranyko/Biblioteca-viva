-- =====================================================
-- DDL — Biblioteca Viva
-- =====================================================
-- Equipe: Raniery Chiarelli (2840482321007) — Vinicius Rocha (2840482523051) — Isaac Leonardo da Silva (2840482421016)
-- Trilha: B
-- Origem: Banco de temas nº 9 — Controle de biblioteca comunitária
-- Data: 04/09/2026
-- =====================================================
-- schema.sql — Biblioteca Viva
-- PostgreSQL 15+. Executar em banco vazio: psql -f schema.sql

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL
        CHECK (perfil IN ('leitor', 'atendente', 'administrador')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE autor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(160) NOT NULL
);

CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE livro (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(13) NOT NULL UNIQUE,
    ano_publicacao SMALLINT,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_livro_isbn
        CHECK (char_length(isbn) IN (10,13)),

    CONSTRAINT chk_livro_ano
        CHECK (
            ano_publicacao IS NULL
            OR ano_publicacao >= 1000
        )
);

CREATE TABLE configuracao_biblioteca (
    id SMALLINT PRIMARY KEY,
    prazo_emprestimo_dias SMALLINT NOT NULL
        CHECK (prazo_emprestimo_dias > 0),
    limite_emprestimos SMALLINT NOT NULL
        CHECK (limite_emprestimos > 0),
    valor_multa_dia NUMERIC(10,2) NOT NULL
        CHECK (valor_multa_dia >= 0),
    prazo_retirada_reserva_dias SMALLINT NOT NULL
        CHECK (prazo_retirada_reserva_dias > 0),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_configuracao_unica
        CHECK (id = 1)
);

CREATE TABLE leitor (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE
        REFERENCES usuario(id),
    documento VARCHAR(30) NOT NULL UNIQUE,
    telefone VARCHAR(25),
    cadastrado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE livro_autor (
    livro_id BIGINT NOT NULL
        REFERENCES livro(id),
    autor_id BIGINT NOT NULL
        REFERENCES autor(id),

    PRIMARY KEY (livro_id, autor_id)
);

CREATE TABLE livro_categoria (
    livro_id BIGINT NOT NULL
        REFERENCES livro(id),
    categoria_id BIGINT NOT NULL
        REFERENCES categoria(id),

    PRIMARY KEY (livro_id, categoria_id)
);

CREATE TABLE exemplar (
    id BIGSERIAL PRIMARY KEY,
    livro_id BIGINT NOT NULL
        REFERENCES livro(id),

    codigo_tombo VARCHAR(50) NOT NULL UNIQUE,

    estado_conservacao VARCHAR(20) NOT NULL
        CHECK (
            estado_conservacao IN (
                'novo',
                'bom',
                'regular',
                'danificado'
            )
        ),

    status VARCHAR(20) NOT NULL DEFAULT 'disponivel'
        CHECK (
            status IN (
                'disponivel',
                'emprestado',
                'reservado',
                'inativo'
            )
        ),

    cadastrado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE emprestimo (
    id BIGSERIAL PRIMARY KEY,

    leitor_id BIGINT NOT NULL
        REFERENCES leitor(id),

    exemplar_id BIGINT NOT NULL
        REFERENCES exemplar(id),

    atendente_retirada_id BIGINT NOT NULL
        REFERENCES usuario(id),

    atendente_devolucao_id BIGINT
        REFERENCES usuario(id),

    data_retirada TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    data_prevista_devolucao DATE NOT NULL,

    data_devolucao TIMESTAMPTZ,

    CONSTRAINT chk_emprestimo_data_prevista
        CHECK (
            data_prevista_devolucao >= data_retirada::date
        ),

    CONSTRAINT chk_emprestimo_devolucao
        CHECK (
            data_devolucao IS NULL
            OR data_devolucao >= data_retirada
        )
);

CREATE TABLE reserva (
    id BIGSERIAL PRIMARY KEY,

    leitor_id BIGINT NOT NULL
        REFERENCES leitor(id),

    livro_id BIGINT NOT NULL
        REFERENCES livro(id),

    exemplar_id BIGINT
        REFERENCES exemplar(id),

    data_solicitacao TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    status VARCHAR(20) NOT NULL DEFAULT 'ativa'
        CHECK (
            status IN (
                'ativa',
                'disponivel',
                'atendida',
                'cancelada',
                'expirada'
            )
        ),

    data_disponibilizacao TIMESTAMPTZ,
    data_limite_retirada TIMESTAMPTZ
);

CREATE TABLE multa (
    id BIGSERIAL PRIMARY KEY,

    emprestimo_id BIGINT NOT NULL UNIQUE
        REFERENCES emprestimo(id),

    dias_atraso INTEGER NOT NULL
        CHECK (dias_atraso > 0),

    valor_diario_aplicado NUMERIC(10,2) NOT NULL
        CHECK (valor_diario_aplicado >= 0),

    valor_total NUMERIC(10,2)
        GENERATED ALWAYS AS
        (dias_atraso * valor_diario_aplicado) STORED,

    status VARCHAR(20) NOT NULL DEFAULT 'pendente'
        CHECK (
            status IN (
                'pendente',
                'quitada',
                'cancelada'
            )
        ),

    gerada_em TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    paga_em TIMESTAMPTZ
);

-- Impede dois empréstimos ativos para o mesmo exemplar (E2, história #7)
CREATE UNIQUE INDEX ux_emprestimo_exemplar_ativo
ON emprestimo(exemplar_id)
WHERE data_devolucao IS NULL;

-- Impede reservas duplicadas ativas/disponíveis (E2, história #9)
CREATE UNIQUE INDEX ux_reserva_ativa_livro_leitor
ON reserva(leitor_id, livro_id)
WHERE status IN ('ativa', 'disponivel');

-- Impede que o mesmo exemplar seja separado para duas reservas simultaneamente (E2, história #10)
CREATE UNIQUE INDEX ux_reserva_exemplar_disponivel
ON reserva(exemplar_id)
WHERE status = 'disponivel';

-- Seed de exemplo

-- Configuração única
INSERT INTO configuracao_biblioteca (
    id, prazo_emprestimo_dias, limite_emprestimos, valor_multa_dia, prazo_retirada_reserva_dias
) VALUES (1, 14, 3, 2.50, 2);

-- Usuários (admin, atendentes e leitores)
-- Senha de teste de todos os usuários: password
INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES
('Administrador', 'admin@biblioteca.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'administrador'),
('João Silva', 'joao@biblioteca.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'leitor'),
('Maria Souza', 'maria@biblioteca.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'leitor'),
('Carlos Pereira', 'carlos@biblioteca.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'atendente'),
('Ana Oliveira', 'ana@biblioteca.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'atendente');

-- Leitores
INSERT INTO leitor (usuario_id, documento, telefone) VALUES
(2, '12345678900', '(16)99999-9999'),
(3, '98765432100', '(16)98888-8888');

-- Autores
INSERT INTO autor (nome) VALUES
('Machado de Assis'),
('Clarice Lispector'),
('Jose de Alencar'),
('Graciliano Ramos');

-- Categorias
INSERT INTO categoria (nome) VALUES
('Literatura Brasileira'),
('Romance'),
('Contos'),
('Infantojuvenil'),
('Modernismo');

-- Livros
INSERT INTO livro (titulo, isbn, ano_publicacao, descricao) VALUES
('Dom Casmurro', '1234567890123', 1899, 'Romance clássico da literatura brasileira.'),
('A Hora da Estrela', '9876543210123', 1977, 'Obra marcante de Clarice Lispector.'),
('Iracema', '1111111111111', 1865, 'Romance indianista de José de Alencar.'),
('Vidas Secas', '2222222222222', 1938, 'Obra de Graciliano Ramos sobre a seca no sertão.');

-- Relações livro_autor
INSERT INTO livro_autor (livro_id, autor_id) VALUES
(1,1),
(2,2),
(3,3),
(4,4);

-- Relações livro_categoria
INSERT INTO livro_categoria (livro_id, categoria_id) VALUES
(1,1),(1,2),
(2,1),(2,3),
(3,1),(3,2),
(4,1),(4,5);

-- Exemplares
INSERT INTO exemplar (livro_id, codigo_tombo, estado_conservacao, status) VALUES
(1, 'TOMBO-0001', 'novo', 'disponivel'),
(1, 'TOMBO-0002', 'bom', 'emprestado'),
(2, 'TOMBO-0003', 'regular', 'disponivel'),
(2, 'TOMBO-0004', 'danificado', 'inativo'),
(3, 'TOMBO-0005', 'bom', 'reservado'),
(4, 'TOMBO-0006', 'novo', 'disponivel');

-- Empréstimos: um ativo e atrasado; outro devolvido com um dia de atraso
INSERT INTO emprestimo (
    leitor_id,
    exemplar_id,
    atendente_retirada_id,
    atendente_devolucao_id,
    data_retirada,
    data_prevista_devolucao,
    data_devolucao
) VALUES
(1, 2, 4, NULL, CURRENT_TIMESTAMP - INTERVAL '17 days', CURRENT_DATE - 3, NULL),
(2, 3, 5, 4, CURRENT_TIMESTAMP - INTERVAL '20 days', CURRENT_DATE - 6, CURRENT_TIMESTAMP - INTERVAL '5 days');

-- Reservas: exemplar 5 separado para Maria; João aguarda em seguida na fila da mesma obra
INSERT INTO reserva (
    leitor_id,
    livro_id,
    exemplar_id,
    status,
    data_solicitacao,
    data_disponibilizacao,
    data_limite_retirada
) VALUES
(2, 3, 5, 'disponivel', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '1 day'),
(1, 3, NULL, 'ativa', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, NULL);

-- Multa referente ao empréstimo 2, devolvido com um dia de atraso
INSERT INTO multa (emprestimo_id, dias_atraso, valor_diario_aplicado, status, paga_em)
VALUES
(2, 1, 2.50, 'quitada', CURRENT_TIMESTAMP - INTERVAL '5 days');
