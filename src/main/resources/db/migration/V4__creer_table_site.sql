CREATE TABLE referential.site (
    id                       uuid                     NOT NULL,
    tenant_id                uuid                     NOT NULL,
    code                     varchar(50)              NOT NULL,
    libelle                  varchar(255)             NOT NULL,
    client_id                uuid,
    localisation             geography(Point,4326)    NOT NULL,
    adresse                  varchar(500),
    horaires_json            text,
    contraintes_acces_json   text,
    actif                    boolean                  NOT NULL DEFAULT true,
    created_at               timestamptz              NOT NULL,
    created_by               varchar(100)             NOT NULL,
    updated_at               timestamptz              NOT NULL,
    updated_by               varchar(100)             NOT NULL,
    version                  bigint                   NOT NULL DEFAULT 0,
    CONSTRAINT pk_site PRIMARY KEY (id),
    CONSTRAINT uq_site_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT fk_site_client FOREIGN KEY (client_id)
        REFERENCES referential.client (id) ON DELETE SET NULL
);

-- Index GiST sur la localisation géographique, utilisé par les requêtes de proximité.
CREATE INDEX idx_site_localisation_gist ON referential.site USING gist (localisation);

-- Index trigram pour la recherche approchée sur le libellé du site.
CREATE INDEX idx_site_libelle_trgm ON referential.site USING gin (libelle gin_trgm_ops);
