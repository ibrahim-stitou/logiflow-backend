# Module `referential` — sites, clients, marchandises

## Rôle

Données de référence partagées par tout le TMS.

## Modèle

- `Site` :
  - adresse, coordonnées GPS (`GeoPoint`), `Horaires` d'ouverture ;
  - contraintes d'accès : gabarit, quai, hayon…
- `Client` : raison sociale, contacts, sites.
- `Marchandise` : catalogue (nature, conditionnement, ADR, température).

## API REST

- `/api/v1/sites`
- `/api/v1/clients`
- `/api/v1/marchandises`

Toutes avec recherche paginée.

## API publique

| Interface | Principaux usages |
|---|---|
| `SiteApi` | `planning` (arrêts, coordonnées), `ai` (planification) |
| `ClientApi` | `order`, `ai` (copilote) |
| `MarchandiseApi` | `dossier` |

## Dépendances

Aucune (hors `shared`). C'est le module de référence de la structure hexagonale.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
