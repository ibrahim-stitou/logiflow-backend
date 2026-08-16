CREATE TABLE referential.client (
    id              uuid            NOT NULL,
    tenant_id       uuid            NOT NULL,
    code            varchar(50)     NOT NULL,
    raison_sociale  varchar(255)    NOT NULL,
    actif           boolean         NOT NULL DEFAULT true,
    created_at      timestamptz     NOT NULL,
    created_by      varchar(100)    NOT NULL,
    updated_at      timestamptz     NOT NULL,
    updated_by      varchar(100)    NOT NULL,
    version         bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_client PRIMARY KEY (id),
    CONSTRAINT uq_client_tenant_code UNIQUE (tenant_id, code)
);

-- Index trigram pour la recherche approchée sur la raison sociale.
CREATE INDEX idx_client_raison_sociale_trgm ON referential.client USING gin (raison_sociale gin_trgm_ops);
