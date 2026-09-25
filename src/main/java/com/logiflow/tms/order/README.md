# Module `order` — commandes (schéma SQL `commande`)

## Rôle

Point d'entrée **commercial** : la demande d'un client, avant sa transformation en dossiers de
transport.

## Modèle

- **`Commande`** : référence, client, lignes (marchandises, quantités), dates souhaitées,
  statut.
- **`StatutCommande`** : `RECUE` → `CONFIRMEE`, ou `ANNULEE`

## API REST

- `/api/v1/commandes` : CRUD et recherche.
- `/{id}/confirmer` et `/{id}/annuler` : changements de statut.

## API publique

`CommandeApi`, utilisée par `dossier` (création des dossiers depuis une commande) et `ai`
(copilote).

## Dépendances

`referential` (clients, marchandises). Le schéma SQL s'appelle `commande`, car `ORDER` est un
mot réservé.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
