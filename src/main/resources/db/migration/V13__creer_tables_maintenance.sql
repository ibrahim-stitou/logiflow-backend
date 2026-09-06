CREATE TABLE maintenance.plan_entretien (
    id                  uuid            NOT NULL,
    vehicule_id         uuid            NOT NULL,
    libelle             varchar(255)    NOT NULL,
    periodicite_km      integer,
    periodicite_mois    integer,
    seuil_alerte_km     integer         NOT NULL DEFAULT 0,
    duree_estimee_min   integer         NOT NULL DEFAULT 0,
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_plan_entretien PRIMARY KEY (id)
);

CREATE INDEX idx_plan_entretien_vehicule ON maintenance.plan_entretien (vehicule_id);

CREATE TABLE maintenance.ordre_travail (
    id                  uuid            NOT NULL,
    vehicule_id         uuid            NOT NULL,
    type_intervention   varchar(30)     NOT NULL,
    statut              varchar(20)     NOT NULL,
    date_planifiee      timestamp       NOT NULL,
    duree_reelle_min    integer         NOT NULL DEFAULT 0,
    cout_montant        numeric(12,2)   NOT NULL,
    cout_devise         varchar(3)      NOT NULL,
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_ordre_travail PRIMARY KEY (id)
);

CREATE INDEX idx_ordre_travail_vehicule ON maintenance.ordre_travail (vehicule_id);
CREATE INDEX idx_ordre_travail_statut ON maintenance.ordre_travail (statut);

CREATE TABLE maintenance.score_sante (
    id                          uuid            NOT NULL,
    vehicule_id                 uuid            NOT NULL,
    calcule_le                  date            NOT NULL,
    score                       double precision NOT NULL,
    statut                      varchar(20)     NOT NULL,
    km_avant_echeance           integer         NOT NULL,
    date_echeance_projetee      date            NOT NULL,
    recommandation              varchar(1000),
    created_at                  timestamptz     NOT NULL,
    created_by                  varchar(100)    NOT NULL,
    updated_at                  timestamptz     NOT NULL,
    updated_by                  varchar(100)    NOT NULL,
    version                     bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_score_sante PRIMARY KEY (id),
    CONSTRAINT ck_score_sante_score CHECK (score >= 0 AND score <= 100)
);

CREATE INDEX idx_score_sante_vehicule_date ON maintenance.score_sante (vehicule_id, calcule_le DESC);
