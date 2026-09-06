CREATE TABLE dossier.ligne_marchandise (
    id              uuid            NOT NULL,
    dossier_id      uuid            NOT NULL,
    marchandise_id  uuid            NOT NULL,
    poids_kg        double precision NOT NULL,
    volume_m3       double precision NOT NULL,
    nb_colis        integer         NOT NULL DEFAULT 0,
    classe_adr      varchar(20),
    numero_onu      varchar(20),
    gerbable        boolean,
    created_at      timestamptz     NOT NULL,
    created_by      varchar(100)    NOT NULL,
    updated_at      timestamptz     NOT NULL,
    updated_by      varchar(100)    NOT NULL,
    version         bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_ligne_marchandise PRIMARY KEY (id),
    CONSTRAINT fk_ligne_marchandise_dossier FOREIGN KEY (dossier_id)
        REFERENCES dossier.dossier_transport (id) ON DELETE CASCADE,
    CONSTRAINT fk_ligne_marchandise_marchandise FOREIGN KEY (marchandise_id)
        REFERENCES referential.marchandise (id),
    CONSTRAINT ck_ligne_marchandise_poids_positif CHECK (poids_kg >= 0),
    CONSTRAINT ck_ligne_marchandise_volume_positif CHECK (volume_m3 >= 0)
);

CREATE INDEX idx_ligne_marchandise_dossier ON dossier.ligne_marchandise (dossier_id);
