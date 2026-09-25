# Module `planning` — voyages

## Rôle

Construction et suivi des **voyages** : quels dossiers, dans quel ordre, avec quel véhicule,
quelle remorque et quels chauffeurs, et à quelles heures. Le module porte aussi le **moteur de
conformité** qui décide si un voyage peut être créé.

## Modèle

- **`Voyage`** :
  - référence (`VOY-AAAA-nnnnnn`), type (`SIMPLE`, `GROUPAGE`, `RAMASSE`, `DISTRIBUTION`,
    `NAVETTE`), portée (`NATIONAL`, `INTERNATIONAL`) ;
  - départ et arrivée prévus, trajet (étapes, distances, durées) ;
  - véhicule, remorque, affectations de chauffeurs (`TITULAIRE`, `RENFORT`).
- **`ArretVoyage`** : passage ordonné à un site, avec heures d'arrivée et de départ estimées,
  chargements et déchargements des dossiers.
- **`StatutVoyage`** : `BROUILLON` → `PLANIFIE` → `AFFECTE` → `EN_COURS` → `TERMINE` →
  `CLOTURE`.

## API REST

- `/api/v1/voyages` : CRUD, recherche, `/{id}/statut`, `/{id}/arrets`, `/{id}/capacite`.
- `/{voyageId}/dossiers` (+ `/check`) : ajout d'un dossier à un voyage existant.
- `/conformite` : contrôle **à blanc** d'un projet de voyage.
- `/ressources-disponibles?debut&fin` : véhicules, remorques et chauffeurs libres sur la période.

## API publique

`VoyageApi` :

- recherche ;
- ressources occupées sur une période ;
- `evaluerConformite`, qui sert à revalider les propositions de l'agent IA ;
- activité des véhicules et remorques, qui mesure leur usage pour la maintenance prédictive.

## Dépendances

`dossier`, `driver`, `fleet`, `referential`, `maintenance` (engins retenus à l'atelier).

## Règles clés (`ConformiteVoyageService`)

**Bloquant** :

- ressource déjà prise **sur la période** (chevauchement avec un autre voyage ou un OT
  d'atelier) ;
- document expiré à la date de départ ;
- tracteur sans remorque ;
- carrosserie ou température incompatible ;
- capacité dépassée sur un tronçon ;
- permis, ADR ou passeport manquant ;
- solde de conduite insuffisant pour l'équipage.

**Avertissement** : fenêtre horaire hors période.

Voir l'ADR 0005 (planification) et [docs/agents-ia.md](../../../../../../../docs/agents-ia.md) §5
(agent de planification).

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
