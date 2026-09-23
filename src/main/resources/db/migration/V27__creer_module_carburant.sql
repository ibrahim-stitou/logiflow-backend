CREATE SCHEMA IF NOT EXISTS carburant;

CREATE TABLE carburant.station (
    id                  uuid            NOT NULL,
    code                varchar(50)     NOT NULL,
    libelle             varchar(255)    NOT NULL,
    adresse             varchar(500),
    actif               boolean         NOT NULL DEFAULT true,
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_station PRIMARY KEY (id),
    CONSTRAINT uq_station_code UNIQUE (code)
);

CREATE INDEX idx_station_libelle_trgm ON carburant.station USING gin (libelle gin_trgm_ops);
CREATE INDEX idx_station_code_trgm ON carburant.station USING gin (code gin_trgm_ops);

CREATE TABLE carburant.prise_carburant (
    id                  uuid            NOT NULL,
    voyage_id           uuid            NOT NULL,
    vehicule_id         uuid,
    remorque_id         uuid,
    station_id          uuid            NOT NULL,
    type_carburant      varchar(20)     NOT NULL,
    litrage             double precision NOT NULL,
    montant_ttc         numeric(12, 2)  NOT NULL,
    date_prise          timestamptz     NOT NULL,
    statut              varchar(20)     NOT NULL DEFAULT 'BROUILLON',
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_prise_carburant PRIMARY KEY (id),
    CONSTRAINT chk_prise_engin_xor CHECK (
        (vehicule_id IS NOT NULL AND remorque_id IS NULL)
        OR (vehicule_id IS NULL AND remorque_id IS NOT NULL)
    ),
    CONSTRAINT fk_prise_voyage FOREIGN KEY (voyage_id)
        REFERENCES planning.voyage (id),
    CONSTRAINT fk_prise_vehicule FOREIGN KEY (vehicule_id)
        REFERENCES fleet.vehicule (id),
    CONSTRAINT fk_prise_remorque FOREIGN KEY (remorque_id)
        REFERENCES fleet.remorque (id),
    CONSTRAINT fk_prise_station FOREIGN KEY (station_id)
        REFERENCES carburant.station (id)
);

CREATE INDEX idx_prise_voyage ON carburant.prise_carburant (voyage_id);
CREATE INDEX idx_prise_statut ON carburant.prise_carburant (statut);
CREATE INDEX idx_prise_date ON carburant.prise_carburant (date_prise DESC);
