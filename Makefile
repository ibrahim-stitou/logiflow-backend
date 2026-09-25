.PHONY: up down build test run format clean logs db-reset

COMPOSE_FILE=docker/docker-compose.yml

## Démarre l'infrastructure locale (PostgreSQL + pgAdmin)
up:
	docker compose -f $(COMPOSE_FILE) up -d

## Démarre Keycloak (realm logiflow importé, port 8081)
keycloak:
	docker compose -f $(COMPOSE_FILE) up -d keycloak

## Arrête l'infrastructure locale
down:
	docker compose -f $(COMPOSE_FILE) down

## Compile le projet sans exécuter les tests
build:
	./mvnw clean package -DskipTests

## Exécute la totalité des tests (unitaires + intégration)
test:
	./mvnw clean verify

## Démarre l'application en profil local
run:
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local

## Applique le formatage de code (Spotless)
format:
	./mvnw spotless:apply

## Nettoie les artefacts de build
clean:
	./mvnw clean

## Affiche les logs du conteneur PostgreSQL
logs:
	docker compose -f $(COMPOSE_FILE) logs -f postgres

## Réinitialise complètement la base de données locale (destructif)
db-reset:
	docker compose -f $(COMPOSE_FILE) down -v
	docker compose -f $(COMPOSE_FILE) up -d
