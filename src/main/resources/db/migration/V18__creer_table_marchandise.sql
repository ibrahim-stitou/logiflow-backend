CREATE TABLE referential.marchandise (
    id              uuid            NOT NULL,
    code            varchar(50)     NOT NULL,
    libelle         varchar(255)    NOT NULL,
    famille         varchar(100),
    classe_adr      varchar(20),
    numero_onu      varchar(20),
    gerbable        boolean         NOT NULL DEFAULT true,
    actif           boolean         NOT NULL DEFAULT true,
    created_at      timestamptz     NOT NULL,
    created_by      varchar(100)    NOT NULL,
    updated_at      timestamptz     NOT NULL,
    updated_by      varchar(100)    NOT NULL,
    version         bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_marchandise PRIMARY KEY (id),
    CONSTRAINT uq_marchandise_code UNIQUE (code)
);

CREATE INDEX idx_marchandise_libelle_trgm ON referential.marchandise USING gin (libelle gin_trgm_ops);
