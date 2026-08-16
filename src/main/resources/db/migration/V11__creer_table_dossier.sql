CREATE TABLE dossier.dossier_transport (
    id                          uuid            NOT NULL,
    tenant_id                   uuid            NOT NULL,
    reference                   varchar(20)     NOT NULL,
    commande_id                 uuid            NOT NULL,
    statut                      varchar(20)     NOT NULL,
    type_transport              varchar(20)     NOT NULL,
    groupable                   boolean         NOT NULL DEFAULT true,
    poids_brut_kg               double precision NOT NULL,
    volume_m3                   double precision NOT NULL,
    nb_palettes                 integer         NOT NULL DEFAULT 0,
    famille_marchandise         varchar(100)    NOT NULL,
    carrosserie_requise         varchar(20),
    temperature_requise         double precision,
    lignes_marchandise_json     text            NOT NULL,
    segments_json               text            NOT NULL,
    documents_json              text,
    created_at                  timestamptz     NOT NULL,
    created_by                  varchar(100)    NOT NULL,
    updated_at                  timestamptz     NOT NULL,
    updated_by                  varchar(100)    NOT NULL,
    version                     bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_dossier_transport PRIMARY KEY (id),
    CONSTRAINT uq_dossier_tenant_reference UNIQUE (tenant_id, reference)
);

CREATE INDEX idx_dossier_commande ON dossier.dossier_transport (commande_id);
CREATE INDEX idx_dossier_statut ON dossier.dossier_transport (statut);
CREATE INDEX idx_dossier_groupable ON dossier.dossier_transport (groupable) WHERE groupable = true;
