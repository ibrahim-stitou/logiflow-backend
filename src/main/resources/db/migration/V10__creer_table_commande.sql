CREATE TABLE commande.commande (
    id               uuid            NOT NULL,
    reference        varchar(20)     NOT NULL,
    client_id        uuid            NOT NULL,
    statut           varchar(20)     NOT NULL,
    date_souhaitee   date            NOT NULL,
    prix_montant     numeric(12,2)   NOT NULL,
    prix_devise      varchar(3)      NOT NULL,
    created_at       timestamptz     NOT NULL,
    created_by       varchar(100)    NOT NULL,
    updated_at       timestamptz     NOT NULL,
    updated_by       varchar(100)    NOT NULL,
    version          bigint          NOT NULL DEFAULT 0,
    CONSTRAINT pk_commande PRIMARY KEY (id),
    CONSTRAINT uq_commande_reference UNIQUE (reference),
    CONSTRAINT ck_commande_prix_positif CHECK (prix_montant >= 0)
);

CREATE INDEX idx_commande_client ON commande.commande (client_id);
CREATE INDEX idx_commande_statut ON commande.commande (statut);
