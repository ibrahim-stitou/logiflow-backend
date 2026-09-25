# Module `fleet` — flotte (véhicules et remorques)

## Rôle

Caractéristiques, statut et compteurs des **véhicules** (tracteurs, porteurs) et des
**remorques**.

## Modèle

- **`Vehicule`** :
  - immatriculation, type (`TRACTEUR`, `PORTEUR`), énergie ;
  - carrosserie (`TAUTLINER`, `FRIGORIFIQUE`, `CITERNE`, `PLATEAU`, `BENNE`) ;
  - charge utile, volume, palettes, PTAC, ADR ;
  - compteurs (kilométrage, heures moteur), année de mise en circulation ;
  - statut.
- **`Remorque`** : immatriculation, type, carrosserie, capacités, groupe froid (heures),
  compteurs, statut.
- **`StatutVehicule`** : `DISPONIBLE`, `RESERVE`, `EN_VOYAGE`, `EN_MAINTENANCE`, `IMMOBILISE`
  (et sortie de flotte).

## API REST

- `/api/v1/vehicules`, `/api/v1/remorques` : CRUD et recherche.
- `/{id}/compteurs` : relevé des compteurs (un compteur ne recule jamais).
- `/{id}/sortie` : sortie de flotte.

## API publique

`VehiculeApi` et `RemorqueApi` :

- **lectures** pour `planning` (planification et conformité), `maintenance`, `ai`,
  `carburant` ;
- **commandes appelées par `maintenance`** : `signalerImmobilisation`,
  `signalerRemiseEnService`, `releverCompteurs`.

## Dépendances

`document` : validité des documents (carte grise, assurance, contrôle technique, ADR) à une date.

## Règles clés

- Un engin `EN_MAINTENANCE` ou `IMMOBILISE` n'est pas affectable à un voyage.
- La remise en service n'est demandée par la maintenance que si l'engin n'est plus retenu (plus
  d'OT immobilisant en cours, plus de sinistre immobilisant ouvert).

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
