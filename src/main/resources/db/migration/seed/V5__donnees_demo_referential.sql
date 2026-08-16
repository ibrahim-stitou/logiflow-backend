-- Jeu de données de démonstration. Ce script n'est appliqué qu'en profils local/dev : voir
-- spring.flyway.locations dans application-local.yml / application-dev.yml, qui ajoute
-- classpath:db/migration/seed à l'emplacement standard classpath:db/migration.

INSERT INTO referential.client (id, tenant_id, code, raison_sociale, actif,
    created_at, created_by, updated_at, updated_by, version)
VALUES
    ('11111111-1111-1111-1111-111111111111', '00000000-0000-0000-0000-000000000000',
     'CLI-DEMO-001', 'Transports Demo SARL', true, now(), 'system', now(), 'system', 0);

INSERT INTO referential.site (id, tenant_id, code, libelle, client_id, localisation,
    adresse, horaires_json, contraintes_acces_json, actif,
    created_at, created_by, updated_at, updated_by, version)
VALUES
    ('22222222-2222-2222-2222-222222222222', '00000000-0000-0000-0000-000000000000',
     'SITE-DEMO-PARIS', 'Entrepôt Paris Nord', '11111111-1111-1111-1111-111111111111',
     ST_SetSRID(ST_MakePoint(2.3522, 48.8566), 4326)::geography,
     '10 rue de la Logistique, 75018 Paris', '[]', '{"interditPoidsLourd":false}', true,
     now(), 'system', now(), 'system', 0);
