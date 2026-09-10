CREATE TABLE commande.ligne_commande (
    id              uuid            NOT NULL,
    commande_id     uuid            NOT NULL,
    marchandise_id  uuid            NOT NULL,
    poids_kg        double precision NOT NULL,
    volume_m3       double precision NOT NULL,
    nb_colis        integer         NOT NULL DEFAULT 0,
    created_at      timestamptz     NOT NULL,
    created_by      varchar(100)    NOT NULL,
    updated_at      timestamptz     NOT NULL,
    updated_by      varchar(100)    NOT NULL,
    version         bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_ligne_commande PRIMARY KEY (id),
    CONSTRAINT fk_ligne_commande_commande FOREIGN KEY (commande_id)
        REFERENCES commande.commande (id) ON DELETE CASCADE,
    CONSTRAINT fk_ligne_commande_marchandise FOREIGN KEY (marchandise_id)
        REFERENCES referential.marchandise (id),
    CONSTRAINT ck_ligne_commande_poids_positif CHECK (poids_kg >= 0),
    CONSTRAINT ck_ligne_commande_volume_positif CHECK (volume_m3 >= 0)
);

CREATE INDEX idx_ligne_commande_commande ON commande.ligne_commande (commande_id);
