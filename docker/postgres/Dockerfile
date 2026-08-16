# Image PostgreSQL 18 + PostGIS 3.6 (officielle) enrichie avec pgvector.
# Nécessaire car aucune image officielle ne combine nativement PostGIS et pgvector.
FROM postgis/postgis:18-3.6

# v0.8.0 ne compile pas contre PostgreSQL 18 (signature de vacuum_delay_point changée côté PG18) ;
# v0.8.1 ajoute le support PG18. TODO revérifier la dernière version stable avant mise en prod.
ARG PGVECTOR_VERSION=v0.8.1

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        build-essential \
        postgresql-server-dev-18 \
        git \
        ca-certificates \
    && git clone --branch ${PGVECTOR_VERSION} --depth 1 https://github.com/pgvector/pgvector.git /tmp/pgvector \
    && cd /tmp/pgvector \
    && make \
    && make install \
    && cd / \
    && rm -rf /tmp/pgvector \
    && apt-get purge -y --auto-remove build-essential git postgresql-server-dev-18 \
    && rm -rf /var/lib/apt/lists/*
