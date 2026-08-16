-- Extensions activées au premier démarrage du conteneur (docker-entrypoint-initdb.d).
-- Idempotent : sans effet si les extensions existent déjà.

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS vector;
