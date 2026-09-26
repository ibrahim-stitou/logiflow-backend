# syntax=docker/dockerfile:1
#
# Image Keycloak de production pour LogiFlow : build optimisé (base PostgreSQL, santé et métriques
# activées) et thème de connexion LogiFlow intégré. Publiée par la CI sur GHCR
# (logiflow-keycloak) et déployée par logiflow-infra ; le realm est importé au démarrage depuis un
# fichier généré par l'infra (URL de production).
#
# En développement local, docker-compose.yml utilise l'image officielle en mode start-dev avec le
# thème monté en volume : cette image n'est pas nécessaire.

ARG KEYCLOAK_VERSION=26.0.7

FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS builder
ENV KC_DB=postgres \
    KC_HEALTH_ENABLED=true \
    KC_METRICS_ENABLED=true
COPY themes/logiflow /opt/keycloak/themes/logiflow
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION}
COPY --from=builder /opt/keycloak/ /opt/keycloak/
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized", "--import-realm"]
