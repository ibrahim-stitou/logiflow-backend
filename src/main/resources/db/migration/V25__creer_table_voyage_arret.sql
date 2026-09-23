-- Arrêts ordonnés sur l'itinéraire d'un voyage.
-- Distinct des Segments dossier (chargement/déchargement sur Site) et des Étapes trajet (trajet_json).
CREATE TABLE planning.voyage_arret (
    id                  uuid                NOT NULL,
    voyage_id           uuid                NOT NULL,
    indice_sequence     integer             NOT NULL,
    libelle             varchar(255)        NOT NULL,
    latitude            double precision    NOT NULL,
    longitude           double precision    NOT NULL,
    site_id             uuid,
    est_original        boolean             NOT NULL DEFAULT true,
    created_at          timestamptz         NOT NULL,
    created_by          varchar(100)        NOT NULL,
    updated_at          timestamptz         NOT NULL,
    updated_by          varchar(100)        NOT NULL,
    version             bigint              NOT NULL DEFAULT 0,
    CONSTRAINT pk_voyage_arret PRIMARY KEY (id),
    CONSTRAINT fk_voyage_arret_voyage FOREIGN KEY (voyage_id)
        REFERENCES planning.voyage (id),
    CONSTRAINT fk_voyage_arret_site FOREIGN KEY (site_id)
        REFERENCES referential.site (id) ON DELETE SET NULL,
    CONSTRAINT ck_voyage_arret_indice_sequence CHECK (indice_sequence >= 0),
    CONSTRAINT uq_voyage_arret_sequence UNIQUE (voyage_id, indice_sequence)
);

CREATE INDEX idx_voyage_arret_voyage ON planning.voyage_arret (voyage_id, indice_sequence);

-- Liaison dossier ↔ arrêts voyage pour la capacité remorque par tronçon.
-- Nullable : renseigné lors de la planification ou de l'ajout au voyage.
-- poids_brut_kg et volume_m3 existent déjà sur dossier_transport — pas de colonnes dupliquées.
ALTER TABLE dossier.dossier_transport
    ADD COLUMN arret_chargement_id   uuid,
    ADD COLUMN arret_dechargement_id uuid,
    ADD CONSTRAINT fk_dossier_arret_chargement FOREIGN KEY (arret_chargement_id)
        REFERENCES planning.voyage_arret (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_dossier_arret_dechargement FOREIGN KEY (arret_dechargement_id)
        REFERENCES planning.voyage_arret (id) ON DELETE SET NULL;

CREATE INDEX idx_dossier_arret_chargement ON dossier.dossier_transport (arret_chargement_id)
    WHERE arret_chargement_id IS NOT NULL;

CREATE INDEX idx_dossier_arret_dechargement ON dossier.dossier_transport (arret_dechargement_id)
    WHERE arret_dechargement_id IS NOT NULL;
