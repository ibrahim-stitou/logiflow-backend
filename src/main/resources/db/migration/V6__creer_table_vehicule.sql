CREATE TABLE fleet.vehicule (
    id                uuid            NOT NULL,
    tenant_id         uuid            NOT NULL,
    immatriculation   varchar(20)     NOT NULL,
    type              varchar(20)     NOT NULL,
    ptac_kg           double precision NOT NULL,
    charge_utile_kg   double precision NOT NULL,
    kilometrage       integer         NOT NULL DEFAULT 0,
    heures_moteur     integer         NOT NULL DEFAULT 0,
    statut            varchar(20)     NOT NULL,
    documents_json    text,
    created_at        timestamptz     NOT NULL,
    created_by        varchar(100)    NOT NULL,
    updated_at        timestamptz     NOT NULL,
    updated_by        varchar(100)    NOT NULL,
    version           bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_vehicule PRIMARY KEY (id),
    CONSTRAINT uq_vehicule_tenant_immat UNIQUE (tenant_id, immatriculation),
    CONSTRAINT ck_vehicule_ptac_positif CHECK (ptac_kg > 0),
    CONSTRAINT ck_vehicule_charge_utile_positive CHECK (charge_utile_kg > 0)
);

CREATE INDEX idx_vehicule_statut ON fleet.vehicule (statut);
