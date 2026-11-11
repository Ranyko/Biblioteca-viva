CREATE TABLE clube_leitura (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(160) NOT NULL,
    descricao TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ativo'
        CHECK (status IN ('ativo', 'encerrado')),
    capacidade_maxima SMALLINT NOT NULL
        CHECK (capacidade_maxima > 0),
    periodicidade VARCHAR(80),
    responsavel_id BIGINT NOT NULL
        REFERENCES usuario(id) ON DELETE RESTRICT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE clube_leitura_categoria (
    clube_id BIGINT NOT NULL
        REFERENCES clube_leitura(id) ON DELETE RESTRICT,
    categoria_id BIGINT NOT NULL
        REFERENCES categoria(id) ON DELETE RESTRICT,

    PRIMARY KEY (clube_id, categoria_id)
);

CREATE TABLE encontro_clube (
    id BIGSERIAL PRIMARY KEY,
    clube_id BIGINT NOT NULL
        REFERENCES clube_leitura(id) ON DELETE RESTRICT,
    livro_id BIGINT NOT NULL
        REFERENCES livro(id) ON DELETE RESTRICT,
    data_hora TIMESTAMPTZ NOT NULL,
    local_ou_acesso TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'aberto'
        CHECK (status IN ('aberto', 'cancelado', 'concluido')),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inscricao_encontro (
    id BIGSERIAL PRIMARY KEY,
    leitor_id BIGINT NOT NULL
        REFERENCES leitor(id) ON DELETE RESTRICT,
    encontro_id BIGINT NOT NULL
        REFERENCES encontro_clube(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'confirmada'
        CHECK (status IN ('confirmada', 'cancelada')),
    inscrito_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelado_em TIMESTAMPTZ,
    presente BOOLEAN,
    presenca_registrada_em TIMESTAMPTZ,

    CONSTRAINT ux_inscricao_encontro_leitor
        UNIQUE (encontro_id, leitor_id),
    CONSTRAINT chk_inscricao_cancelamento
        CHECK (
            (status = 'confirmada' AND cancelado_em IS NULL)
            OR (status = 'cancelada' AND cancelado_em IS NOT NULL)
        ),
    CONSTRAINT chk_inscricao_presenca
        CHECK (
            (presente IS NULL AND presenca_registrada_em IS NULL)
            OR (presente IS NOT NULL AND presenca_registrada_em IS NOT NULL)
        )
);

CREATE INDEX ix_clube_leitura_status_nome
    ON clube_leitura(status, nome);

CREATE INDEX ix_clube_leitura_categoria_categoria
    ON clube_leitura_categoria(categoria_id, clube_id);

CREATE INDEX ix_encontro_clube_calendario
    ON encontro_clube(clube_id, data_hora)
    WHERE status = 'aberto';

CREATE INDEX ix_inscricao_encontro_confirmada
    ON inscricao_encontro(encontro_id)
    WHERE status = 'confirmada';

CREATE INDEX ix_inscricao_leitor
    ON inscricao_encontro(leitor_id, inscrito_em DESC);
