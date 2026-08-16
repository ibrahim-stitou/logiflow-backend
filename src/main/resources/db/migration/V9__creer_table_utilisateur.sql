CREATE TABLE iam.utilisateur (
    id            uuid            NOT NULL,
    tenant_id     uuid            NOT NULL,
    login         varchar(100)    NOT NULL,
    email         varchar(255)    NOT NULL,
    roles_json    text            NOT NULL,
    actif         boolean         NOT NULL DEFAULT true,
    created_at    timestamptz     NOT NULL,
    created_by    varchar(100)    NOT NULL,
    updated_at    timestamptz     NOT NULL,
    updated_by    varchar(100)    NOT NULL,
    version       bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_utilisateur PRIMARY KEY (id),
    CONSTRAINT uq_utilisateur_tenant_login UNIQUE (tenant_id, login)
);

CREATE INDEX idx_utilisateur_email ON iam.utilisateur (email);
