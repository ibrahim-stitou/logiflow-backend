-- Refonte du module maintenance (ADR 0006) : ordres de travail détaillés (engin véhicule ou
-- remorque, lignes de coût, réalisation), plans avec dernière réalisation, prestataires,
-- contrats d'assurance et sinistres. Les anciennes tables sont recréées (données de démo
-- uniquement, rechargées par le seed V32). Montants en euros.

DROP TABLE IF EXISTS maintenance.ordre_travail;
DROP TABLE IF EXISTS maintenance.plan_entretien;

CREATE TABLE maintenance.prestataire (
    id                  uuid            NOT NULL,
    code                varchar(30)     NOT NULL,
    raison_sociale      varchar(255)    NOT NULL,
    type_prestataire    varchar(30)     NOT NULL,
    siret               varchar(20),
    contact_nom         varchar(150),
    telephone           varchar(30),
    email               varchar(255),
    adresse             varchar(500),
    notes               text,
    actif               boolean         NOT NULL DEFAULT true,
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_prestataire PRIMARY KEY (id),
    CONSTRAINT uk_prestataire_code UNIQUE (code)
);

CREATE TABLE maintenance.contrat_assurance (
    id                  uuid            NOT NULL,
    assureur_id         uuid            NOT NULL,
    numero_police       varchar(60)     NOT NULL,
    type_contrat        varchar(20)     NOT NULL,
    garanties_json      text            NOT NULL,
    franchise           numeric(12,2),
    prime_annuelle      numeric(12,2),
    date_effet          date            NOT NULL,
    date_echeance       date            NOT NULL,
    engins_json         text            NOT NULL,
    actif               boolean         NOT NULL DEFAULT true,
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_contrat_assurance PRIMARY KEY (id),
    CONSTRAINT fk_contrat_assureur FOREIGN KEY (assureur_id) REFERENCES maintenance.prestataire (id),
    CONSTRAINT ck_contrat_montants CHECK (coalesce(franchise, 0) >= 0 AND coalesce(prime_annuelle, 0) >= 0)
);

CREATE TABLE maintenance.plan_entretien (
    id                      uuid            NOT NULL,
    type_engin              varchar(20)     NOT NULL,
    engin_id                uuid            NOT NULL,
    libelle                 varchar(255)    NOT NULL,
    type_intervention       varchar(30)     NOT NULL,
    periodicite_km          integer,
    periodicite_mois        integer,
    periodicite_heures      integer,
    seuil_alerte_km         integer         NOT NULL DEFAULT 0,
    seuil_alerte_jours      integer         NOT NULL DEFAULT 0,
    duree_estimee_min       integer         NOT NULL DEFAULT 0,
    cout_estime             numeric(12,2),
    prestataire_id          uuid,
    actif                   boolean         NOT NULL DEFAULT true,
    derniere_date           date,
    derniere_km             integer,
    derniere_heures         integer,
    created_at              timestamptz     NOT NULL,
    created_by              varchar(100)    NOT NULL,
    updated_at              timestamptz     NOT NULL,
    updated_by              varchar(100)    NOT NULL,
    version                 bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_plan_entretien PRIMARY KEY (id),
    CONSTRAINT fk_plan_prestataire FOREIGN KEY (prestataire_id) REFERENCES maintenance.prestataire (id),
    CONSTRAINT ck_plan_periodicite CHECK (
        periodicite_km IS NOT NULL OR periodicite_mois IS NOT NULL OR periodicite_heures IS NOT NULL)
);

CREATE INDEX idx_plan_entretien_engin ON maintenance.plan_entretien (engin_id);

CREATE TABLE maintenance.sinistre (
    id                          uuid            NOT NULL,
    reference                   varchar(20)     NOT NULL,
    vehicule_id                 uuid,
    remorque_id                 uuid,
    chauffeur_id                uuid,
    voyage_id                   uuid,
    date_survenance             timestamp       NOT NULL,
    lieu                        varchar(500),
    latitude                    double precision,
    longitude                   double precision,
    type_sinistre               varchar(30)     NOT NULL,
    gravite                     varchar(20)     NOT NULL,
    responsabilite              varchar(20)     NOT NULL,
    description                 text            NOT NULL,
    constat_amiable             boolean         NOT NULL DEFAULT false,
    rapport_police              boolean         NOT NULL DEFAULT false,
    blesses                     boolean         NOT NULL DEFAULT false,
    engin_immobilise            boolean         NOT NULL DEFAULT false,
    tiers_nom                   varchar(255),
    tiers_immatriculation       varchar(20),
    tiers_assureur              varchar(255),
    tiers_numero_police         varchar(60),
    contrat_id                  uuid,
    numero_dossier_assureur     varchar(60),
    date_declaration_assureur   date,
    expert_id                   uuid,
    date_expertise              date,
    estimation_dommages         numeric(12,2),
    franchise                   numeric(12,2),
    indemnite                   numeric(12,2),
    statut                      varchar(20)     NOT NULL,
    date_cloture                date,
    created_at                  timestamptz     NOT NULL,
    created_by                  varchar(100)    NOT NULL,
    updated_at                  timestamptz     NOT NULL,
    updated_by                  varchar(100)    NOT NULL,
    version                     bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_sinistre PRIMARY KEY (id),
    CONSTRAINT uk_sinistre_reference UNIQUE (reference),
    CONSTRAINT fk_sinistre_contrat FOREIGN KEY (contrat_id) REFERENCES maintenance.contrat_assurance (id),
    CONSTRAINT fk_sinistre_expert FOREIGN KEY (expert_id) REFERENCES maintenance.prestataire (id),
    CONSTRAINT ck_sinistre_engin CHECK (vehicule_id IS NOT NULL OR remorque_id IS NOT NULL),
    CONSTRAINT ck_sinistre_montants CHECK (
        coalesce(estimation_dommages, 0) >= 0 AND coalesce(franchise, 0) >= 0
        AND coalesce(indemnite, 0) >= 0)
);

CREATE INDEX idx_sinistre_vehicule ON maintenance.sinistre (vehicule_id);
CREATE INDEX idx_sinistre_remorque ON maintenance.sinistre (remorque_id);
CREATE INDEX idx_sinistre_date ON maintenance.sinistre (date_survenance DESC);

CREATE TABLE maintenance.ordre_travail (
    id                  uuid            NOT NULL,
    reference           varchar(20)     NOT NULL,
    type_engin          varchar(20)     NOT NULL,
    engin_id            uuid            NOT NULL,
    origine             varchar(20)     NOT NULL,
    plan_id             uuid,
    sinistre_id         uuid,
    type_intervention   varchar(30)     NOT NULL,
    nature              varchar(20)     NOT NULL,
    priorite            varchar(10)     NOT NULL,
    titre               varchar(255)    NOT NULL,
    description         text,
    prestataire_id      uuid,
    debut_planifie      timestamp       NOT NULL,
    fin_planifiee       timestamp,
    immobilisation      boolean         NOT NULL DEFAULT true,
    budget_estime       numeric(12,2),
    statut              varchar(20)     NOT NULL,
    lignes_json         text            NOT NULL,
    total_ht            numeric(12,2)   NOT NULL DEFAULT 0,
    total_ttc           numeric(12,2)   NOT NULL DEFAULT 0,
    debut_reel          timestamp,
    fin_reelle          timestamp,
    kilometrage         integer,
    heures              integer,
    diagnostic          text,
    travaux_realises    text,
    intervenant         varchar(150),
    numero_facture      varchar(60),
    date_facture        date,
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_ordre_travail PRIMARY KEY (id),
    CONSTRAINT uk_ordre_travail_reference UNIQUE (reference),
    CONSTRAINT fk_ot_plan FOREIGN KEY (plan_id) REFERENCES maintenance.plan_entretien (id),
    CONSTRAINT fk_ot_sinistre FOREIGN KEY (sinistre_id) REFERENCES maintenance.sinistre (id),
    CONSTRAINT fk_ot_prestataire FOREIGN KEY (prestataire_id) REFERENCES maintenance.prestataire (id),
    CONSTRAINT ck_ot_montants CHECK (total_ht >= 0 AND total_ttc >= 0 AND coalesce(budget_estime, 0) >= 0)
);

CREATE INDEX idx_ot_engin ON maintenance.ordre_travail (engin_id);
CREATE INDEX idx_ot_statut ON maintenance.ordre_travail (statut);
CREATE INDEX idx_ot_debut ON maintenance.ordre_travail (debut_planifie DESC);
CREATE INDEX idx_ot_plan ON maintenance.ordre_travail (plan_id);
CREATE INDEX idx_ot_sinistre ON maintenance.ordre_travail (sinistre_id);
