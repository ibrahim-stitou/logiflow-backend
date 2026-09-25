# Module `carburant` — prises de carburant

## Rôle

Suivi des pleins (gazole, GNR, AdBlue) et de la consommation.

## Modèle

- **`PriseCarburant`** :
  - véhicule, voyage éventuel, station, date, type de carburant ;
  - litres, prix, kilométrage ;
  - justificatif, statut (`BROUILLON` → `VALIDEE`).
- **`Station`** : enseigne, localisation.

## API REST

- `/api/v1/prises-carburant` : CRUD, `/{id}/valider`, `/stats`.
- `/api/v1/stations` : CRUD.

## API publique

`CarburantApi` (consommation d'un véhicule sur une période : litres, coût, L/100 km). Utilisée
par `ai` (agent de maintenance : surconsommation ; copilote : `consommation_carburant`).

## Dépendances

- `planning` : voyage associé.
- `document` : justificatif. Le module écoute `DocumentEntiteModificationEvent` pour qu'une prise
  validée ne soit plus modifiable.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
