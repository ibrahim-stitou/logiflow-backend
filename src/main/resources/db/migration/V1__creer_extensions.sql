-- Extensions PostgreSQL requises par LogiFlow. Idempotent (sans effet si déjà installées, par
-- exemple via docker/postgres/init/01-extensions.sql lors du premier démarrage du conteneur).

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS vector;
