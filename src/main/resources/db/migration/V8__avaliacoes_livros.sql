CREATE TABLE avaliacao_livro (
    id BIGSERIAL PRIMARY KEY,
    livro_id BIGINT NOT NULL REFERENCES livro(id),
    leitor_id BIGINT NOT NULL REFERENCES leitor(id),
    nota SMALLINT NOT NULL CHECK (nota BETWEEN 1 AND 5),
    resenha TEXT,
    criada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_avaliacao_livro_resenha
        CHECK (resenha IS NULL OR char_length(btrim(resenha)) BETWEEN 1 AND 1000),
    CONSTRAINT ux_avaliacao_livro_leitor UNIQUE (livro_id, leitor_id)
);

CREATE INDEX idx_avaliacao_livro_recente
    ON avaliacao_livro (livro_id, atualizada_em DESC, id DESC);
