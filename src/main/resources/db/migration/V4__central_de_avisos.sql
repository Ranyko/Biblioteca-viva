ALTER TABLE reserva
    ADD COLUMN ultima_posicao_observada SMALLINT,
    ADD CONSTRAINT chk_reserva_ultima_posicao
        CHECK (ultima_posicao_observada IS NULL OR ultima_posicao_observada > 0);

CREATE TABLE aviso (
    id              BIGSERIAL   NOT NULL,
    usuario_id      BIGINT      NOT NULL,
    tipo            VARCHAR(30) NOT NULL,
    titulo          VARCHAR(120) NOT NULL,
    mensagem        VARCHAR(500) NOT NULL,
    referencia_tipo VARCHAR(30) NULL,
    referencia_id   BIGINT      NULL,
    dia_referencia  DATE        NULL,
    criado_em       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lido_em         TIMESTAMPTZ NULL,
    CONSTRAINT pk_aviso PRIMARY KEY (id),
    CONSTRAINT fk_aviso_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_aviso_tipo CHECK (tipo IN (
        'DEVIDA_AMANHA','VENCIDO','RESERVA_DISPONIVEL','RESERVA_EXPIRANDO',
        'RESERVA_EXPIRADA','MULTA_GERADA','FILA_POSICAO')),
    CONSTRAINT chk_aviso_referencia CHECK (
        (referencia_tipo IS NULL AND referencia_id IS NULL)
     OR (referencia_tipo IN ('emprestimo','reserva','multa') AND referencia_id IS NOT NULL)),
    CONSTRAINT chk_aviso_leitura CHECK (lido_em IS NULL OR lido_em >= criado_em)
);

CREATE UNIQUE INDEX ux_aviso_idempotente        
    ON aviso(usuario_id, tipo, referencia_tipo, referencia_id, dia_referencia)
    NULLS NOT DISTINCT;

CREATE INDEX ix_aviso_usuario_nao_lido ON aviso(usuario_id, criado_em DESC) WHERE lido_em IS NULL;
CREATE INDEX ix_aviso_usuario_criado   ON aviso(usuario_id, criado_em DESC);

CREATE TABLE aviso_preferencia (                     -- linha única, padrão configuracao_biblioteca
    id                 SMALLINT NOT NULL,
    devida_amanha      BOOLEAN  NOT NULL DEFAULT TRUE,
    vencido            BOOLEAN  NOT NULL DEFAULT TRUE,
    reserva_disponivel BOOLEAN  NOT NULL DEFAULT TRUE,
    reserva_expirando  BOOLEAN  NOT NULL DEFAULT TRUE,
    reserva_expirada   BOOLEAN  NOT NULL DEFAULT TRUE,
    multa_gerada       BOOLEAN  NOT NULL DEFAULT TRUE,
    fila_posicao       BOOLEAN  NOT NULL DEFAULT FALSE,   -- nasce desligado (anti-spam)
    atualizado_em      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_aviso_preferencia PRIMARY KEY (id),
    CONSTRAINT chk_aviso_preferencia_unica CHECK (id = 1)
);

INSERT INTO aviso_preferencia (id) VALUES (1);