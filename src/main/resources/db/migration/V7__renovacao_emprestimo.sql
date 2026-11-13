ALTER TABLE configuracao_biblioteca
    ADD COLUMN limite_renovacoes SMALLINT NOT NULL DEFAULT 2
        CHECK (limite_renovacoes >= 0);

ALTER TABLE emprestimo
    ADD COLUMN limite_renovacoes_snapshot SMALLINT NOT NULL DEFAULT 0
        CHECK (limite_renovacoes_snapshot >= 0);

CREATE TABLE renovacao_emprestimo (
    id BIGSERIAL PRIMARY KEY,
    emprestimo_id BIGINT NOT NULL REFERENCES emprestimo(id),
    leitor_id BIGINT NOT NULL REFERENCES leitor(id),
    atendente_renovacao_id BIGINT REFERENCES usuario(id),
    numero_renovacao SMALLINT NOT NULL CHECK (numero_renovacao > 0),
    data_prevista_anterior DATE NOT NULL,
    data_prevista_nova DATE NOT NULL CHECK (data_prevista_nova > data_prevista_anterior),
    renovada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ux_renovacao_emprestimo_numero UNIQUE (emprestimo_id, numero_renovacao)
);