CREATE TABLE tracking.evenement_voyage (
    id                  uuid            NOT NULL,
    tenant_id           uuid            NOT NULL,
    voyage_id           uuid            NOT NULL,
    type_evenement      varchar(30)     NOT NULL,
    horodatage          timestamptz     NOT NULL,
    latitude            double precision,
    longitude           double precision,
    commentaire         varchar(1000),
    created_at          timestamptz     NOT NULL,
    created_by          varchar(100)    NOT NULL,
    updated_at          timestamptz     NOT NULL,
    updated_by          varchar(100)    NOT NULL,
    version             bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_evenement_voyage PRIMARY KEY (id)
);

CREATE INDEX idx_evenement_voyage_voyage_horodatage ON tracking.evenement_voyage (voyage_id, horodatage);
