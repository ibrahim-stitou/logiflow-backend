-- Un schéma PostgreSQL par module métier.
-- "commande" correspond au module `order`, renommé pour éviter le mot réservé SQL ORDER.

CREATE SCHEMA IF NOT EXISTS iam;
CREATE SCHEMA IF NOT EXISTS referential;
CREATE SCHEMA IF NOT EXISTS fleet;
CREATE SCHEMA IF NOT EXISTS driver;
CREATE SCHEMA IF NOT EXISTS commande;
CREATE SCHEMA IF NOT EXISTS dossier;
CREATE SCHEMA IF NOT EXISTS planning;
CREATE SCHEMA IF NOT EXISTS maintenance;
CREATE SCHEMA IF NOT EXISTS tracking;
