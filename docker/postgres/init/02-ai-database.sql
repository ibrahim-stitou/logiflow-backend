-- Base DÉDIÉE au service IA (logiflow-ai-service) : conversations du copilote, messages,
-- appels d'outils, feedback et base de connaissance vectorielle (pgvector).
-- Le backend Spring n'y a aucun accès, et le service IA n'a aucun accès à la base TMS "logiflow".
-- Voir docs/adr/0004-copilote-base-ia-dediee-et-outils.md.
--
-- Exécuté UNIQUEMENT au premier démarrage d'un volume vide (docker-entrypoint-initdb.d). Sur un
-- volume existant, lancer manuellement :
--   docker exec -i logiflow-postgres psql -U logiflow -d logiflow < docker/postgres/init/02-ai-database.sql

SELECT 'CREATE ROLE logiflow_ai LOGIN PASSWORD ''change-me-local-only'''
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'logiflow_ai')\gexec

SELECT 'CREATE DATABASE logiflow_ai OWNER logiflow_ai'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'logiflow_ai')\gexec

REVOKE ALL ON DATABASE logiflow_ai FROM PUBLIC;

\connect logiflow_ai
-- L'extension exige un superutilisateur : créée ici plutôt que dans les migrations Alembic.
CREATE EXTENSION IF NOT EXISTS vector;
CREATE SCHEMA IF NOT EXISTS copilote AUTHORIZATION logiflow_ai;
