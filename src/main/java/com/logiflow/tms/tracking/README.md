# Module `tracking` — suivi d'exécution

## Rôle

Journal des **événements d'exécution** d'un voyage, qui font avancer les statuts des voyages et
des dossiers.

## Modèle

- **`EvenementVoyage`** : voyage, type, date et heure, position (`GeoPoint`), commentaire.
- **`TypeEvenement`** : `DEPART`, `ARRIVEE_CHARGEMENT`, `CHARGEMENT_TERMINE`,
  `ARRIVEE_DECHARGEMENT`, `LIVRAISON_TERMINEE`, `POSITION`, `INCIDENT`.

## API REST

- `/api/v1/evenements-voyage`
- `/api/v1/voyages/{voyageId}/evenements`

## API publique

`TrackingApi` (dernier événement d'un voyage), utilisée par `ai` (copilote).

## Dépendances

`planning`.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
