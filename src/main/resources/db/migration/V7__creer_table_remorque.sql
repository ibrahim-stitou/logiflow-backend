CREATE TABLE fleet.remorque (
    id                      uuid            NOT NULL,
    tenant_id               uuid            NOT NULL,
    immatriculation         varchar(20)     NOT NULL,
    carrosserie             varchar(20)     NOT NULL,
    volume_utile_m3         double precision NOT NULL DEFAULT 0,
    nb_positions_palettes   integer         NOT NULL DEFAULT 0,
    charge_utile_kg         double precision NOT NULL DEFAULT 0,
    groupe_froid            boolean         NOT NULL DEFAULT false,
    temperature_min         double precision,
    temperature_max         double precision,
    statut                  varchar(20)     NOT NULL,
    created_at              timestamptz     NOT NULL,
    created_by              varchar(100)    NOT NULL,
    updated_at              timestamptz     NOT NULL,
    updated_by              varchar(100)    NOT NULL,
    version                 bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_remorque PRIMARY KEY (id),
    CONSTRAINT uq_remorque_tenant_immat UNIQUE (tenant_id, immatriculation),
    CONSTRAINT ck_remorque_temperature_coherente CHECK (temperature_min IS NULL OR temperature_max IS NULL OR temperature_min <= temperature_max)
);

CREATE INDEX idx_remorque_statut ON fleet.remorque (statut);
