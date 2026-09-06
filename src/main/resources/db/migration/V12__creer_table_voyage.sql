CREATE TABLE planning.voyage (
    id                  uuid            NOT NULL,
    reference           varchar(20)     NOT NULL,
    type_voyage         varchar(20)     NOT NULL,
    portee              varchar(20)     NOT NULL,
    statut              varchar(20)     NOT NULL,
    depart_prevu        timestamptz     NOT NULL,
    arrivee_prevue      timestamptz     NOT NULL,
    vehicule_id         uuid            NOT NULL,
    remorque_id         uuid,
    dossier_ids_json    text            NOT NULL,
    trajet_json         text            NOT NULL,
    affectations_json   text            NOT NULL,
    taux_remplissage    double precision NOT NULL DEFAULT 0,
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_voyage PRIMARY KEY (id),
    CONSTRAINT uq_voyage_reference UNIQUE (reference),
    CONSTRAINT ck_voyage_taux_remplissage CHECK (taux_remplissage >= 0)
);

CREATE INDEX idx_voyage_vehicule ON planning.voyage (vehicule_id);
CREATE INDEX idx_voyage_statut ON planning.voyage (statut);
