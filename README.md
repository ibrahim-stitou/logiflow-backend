# LogiFlow TMS — Backend

Backend du Transport Management System **LogiFlow** : affectation des véhicules,
optimisation des chemins, détection de groupage de dossiers de transport et
maintenance prédictive pour le transport routier de marchandises.

Le frontend (Angular 18) est un projet séparé, non inclus ici.

## Prérequis

- JDK 25
- Docker + Docker Compose
- (Maven n'est pas requis : le wrapper `./mvnw` télécharge et utilise la bonne version)

## Démarrage en 3 commandes

```bash
cp .env.example .env
make up
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

L'application démarre sur `http://localhost:8080`, applique les migrations Flyway
et expose la documentation interactive sur `http://localhost:8080/swagger-ui.html`.

Vérifier que tout fonctionne :

```bash
curl http://localhost:8080/actuator/health
```

## Structure des packages

Monolithe modulaire (Spring Modulith). Chaque module métier est un sous-package direct
de `com.logiflow.tms`, structuré en 4 couches hexagonales :

```
com.logiflow.tms.<module>/
  api/             contrat public du module (DTO publics, interfaces exposées, événements)
  domain/          modèle métier pur, aucune dépendance framework
  application/     cas d'utilisation, orchestration, transactions
  infrastructure/  adaptateurs web (REST) et persistance (JPA)
```

Modules : `shared`, `iam`, `referential`, `fleet`, `driver`, `order`, `dossier`,
`planning`, `maintenance`, `tracking`.

Le module `referential` (sous-domaine Site/Client) est implémenté intégralement et sert
de modèle de référence — voir [docs/architecture.md](docs/architecture.md) et la section
"Prochaines étapes" en fin de génération pour dupliquer ce modèle vers les autres modules.

## Conventions

Voir [docs/conventions.md](docs/conventions.md) : nommage, style de commits, stratégie
de branches, definition of done.

## Documentation

- [docs/architecture.md](docs/architecture.md) — architecture modulaire + hexagonale, schéma des dépendances
- [docs/conventions.md](docs/conventions.md) — conventions de code et de collaboration
- [docs/adr/0001-monolithe-modulaire.md](docs/adr/0001-monolithe-modulaire.md) — décision d'architecture

## Commandes utiles (`Makefile`)

| Commande        | Effet                                                    |
|-----------------|-----------------------------------------------------------|
| `make up`       | Démarre PostgreSQL (+ pgAdmin) via Docker Compose         |
| `make down`     | Arrête l'infrastructure locale                            |
| `make build`    | Compile sans exécuter les tests                           |
| `make test`     | Exécute la totalité des tests (`./mvnw clean verify`)     |
| `make run`      | Démarre l'application en profil `local`                   |
| `make format`   | Applique le formatage Spotless                            |
| `make db-reset` | Réinitialise complètement la base locale (destructif)     |

## Tests

- `./mvnw test` : tests unitaires (domaine, application)
- `./mvnw verify` : tests unitaires + intégration (Testcontainers PostgreSQL/PostGIS) +
  tests d'architecture (ArchUnit) + vérification Spring Modulith

## Liens utiles

- Swagger UI : `http://localhost:8080/swagger-ui.html`
- Actuator health : `http://localhost:8080/actuator/health`
- Actuator modulith : `http://localhost:8080/actuator/modulith`
