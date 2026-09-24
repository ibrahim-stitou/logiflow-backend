-- Jeu de données de démonstration (Maroc / MAD). Appliqué en profils local/dev uniquement :
-- spring.flyway.locations dans application-local.yml / application-dev.yml.
-- Après modification des seeds : make db-reset puis redémarrer le backend.

INSERT INTO referential.client (id, code, raison_sociale, actif,
    created_at, created_by, updated_at, updated_by, version)
VALUES
    ('11111111-1111-1111-1111-111111111111',
     'CLI-DEMO-001', 'Logistique Maghreb SARL', true, now(), 'system', now(), 'system', 0);

INSERT INTO referential.site (id, code, libelle, client_id, localisation,
    adresse, horaires_json, contraintes_acces_json, actif,
    created_at, created_by, updated_at, updated_by, version)
VALUES
    ('22222222-2222-2222-2222-222222222222',
     'SITE-DEMO-CASA', 'Hub Casablanca Ain Sebaa', '11111111-1111-1111-1111-111111111111',
     ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)::geography,
     'Zone industrielle Ain Sebaa, Casablanca 20250', '[]', '{"interditPoidsLourd":false}', true,
     now(), 'system', now(), 'system', 0);
