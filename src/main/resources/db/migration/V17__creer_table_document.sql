CREATE SCHEMA IF NOT EXISTS document;

-- Table unique partagée par toutes les entités documentables (relation polymorphe) : pas de clé
-- étrangère sur entite_id puisqu'elle peut pointer vers plusieurs tables propriétaires
-- (fleet.vehicule, fleet.remorque, ...).
CREATE TABLE document.document (
    id                  uuid            NOT NULL,
    type_entite         varchar(30)     NOT NULL,
    entite_id           uuid            NOT NULL,
    type_document       varchar(30)     NOT NULL,
    reference           varchar(255),
    url                 varchar(500)    NOT NULL,
    date_expiration     date,
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_document PRIMARY KEY (id)
);

CREATE INDEX idx_document_entite ON document.document (type_entite, entite_id);
