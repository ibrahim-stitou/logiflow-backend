CREATE TABLE driver.chauffeur (
    id                              uuid            NOT NULL,
    matricule                       varchar(30)     NOT NULL,
    nom_complet                     varchar(255)    NOT NULL,
    statut                          varchar(20)     NOT NULL,
    solde_temps_conduite_minutes    bigint          NOT NULL DEFAULT 0,
    habilitations_json              text,
    created_at                      timestamptz     NOT NULL,
    created_by                      varchar(100)    NOT NULL,
    updated_at                      timestamptz     NOT NULL,
    updated_by                      varchar(100)    NOT NULL,
    version                         bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_chauffeur PRIMARY KEY (id),
    CONSTRAINT uq_chauffeur_matricule UNIQUE (matricule),
    CONSTRAINT ck_chauffeur_solde_positif CHECK (solde_temps_conduite_minutes >= 0)
);

CREATE INDEX idx_chauffeur_statut ON driver.chauffeur (statut);
CREATE INDEX idx_chauffeur_nom_complet_trgm ON driver.chauffeur USING gin (nom_complet gin_trgm_ops);
