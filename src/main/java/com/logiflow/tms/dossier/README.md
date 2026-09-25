# Module `dossier` — dossiers de transport

## Rôle

Le **dossier de transport** est l'unité transportable et facturable. Il porte ce qu'il faut
charger, où, quand, et sous quelles contraintes.

## Modèle

- **`DossierTransport`** :
  - référence (`DOS-AAAA-nnnnnn`), commande d'origine, type (`NATIONAL`, `EXPORT`, `IMPORT`) ;
  - lignes de marchandise : poids, volume, palettes, ADR ;
  - carrosserie requise, température ;
  - sites et **fenêtres de chargement et de livraison**.
- **`StatutDossier`** : `CREE` → `PLANIFIE` → `EN_CHARGEMENT` → `CHARGE` → `EN_TRANSIT` →
  `EN_LIVRAISON` → `LIVRE` → `CLOTURE`, ainsi que `INCIDENT`.

## API REST

- `/api/v1/dossiers` : CRUD, recherche, filtre par statut.
- `/{id}/statut` : changement de statut.

## API publique

`DossierApi` :

- consultation ;
- `candidatsPlanification` : dossiers `CREE` dont une fenêtre chevauche une période ;
- mise à jour de statut orchestrée par le voyage (ADR 0002).

Utilisée par `planning` et `ai`.

## Dépendances

`order`, `referential`.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
