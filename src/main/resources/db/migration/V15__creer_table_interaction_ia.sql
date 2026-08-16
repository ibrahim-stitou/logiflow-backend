CREATE SCHEMA IF NOT EXISTS ai;

CREATE TABLE ai.interaction_ia (
    id                  uuid            NOT NULL,
    tenant_id           uuid            NOT NULL,
    type_interaction    varchar(20)     NOT NULL,
    utilisateur_id      varchar(100),
    succes              boolean         NOT NULL,
    duree_ms            bigint          NOT NULL DEFAULT 0,
    resume              varchar(2000),
    erreur              varchar(2000),
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_interaction_ia PRIMARY KEY (id)
);

CREATE INDEX idx_interaction_ia_type_date ON ai.interaction_ia (type_interaction, created_at DESC);
