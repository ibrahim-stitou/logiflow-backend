# Module `maintenance` — maintenance, sinistres, assurance, coûts

## Rôle

Tout ce qui garde la flotte (véhicules **et** remorques) en état de rouler : entretien préventif,
réparations, sinistres, assurance, prestataires, coûts et scores de santé calculés par l'agent
de maintenance prédictive. Décisions et écarts : ADR 0006.

## Modèle

| Agrégat | Contenu |
|---|---|
| `OrdreTravail` | Référence `OT-AAAA-nnnnnn`, engin (`EnginRef`), type, nature (préventif, correctif, réglementaire), priorité, origine (manuelle, plan, sinistre, agent IA, panne), prestataire, dates planifiées et réelles, immobilisation, budget, **lignes de coût** (main-d'œuvre, pièces… avec TVA), clôture (compteurs, travaux, facture) |
| `PlanEntretien` | Engin, type, périodicités (km, mois, heures), seuils d'alerte, durée et coût estimés, **dernière réalisation** ; calcule la prochaine **échéance** (`OK`, `ALERTE`, `ECHU`) |
| `Sinistre` | Référence `SIN-AAAA-nnnnnn`, véhicule et/ou remorque, chauffeur, voyage, circonstances, tiers, gravité, responsabilité, suivi assurance (contrat, dossier, expert, franchise, indemnité), statut |
| `Prestataire` | Garage, concession, carrossier, dépanneur, expert, assureur… |
| `ContratAssurance` | Assureur, police, garanties, franchise, prime, période, engins couverts (flotte ou liste) |
| `ScoreSante` | Score sur 100, statut, km et date avant échéance, recommandation (écrit par l'agent IA) |

Workflows :

- **OT** :
  - `PLANIFIE` → `EN_COURS` ⇄ `EN_ATTENTE_PIECES` → `TERMINE`, ou `ANNULE` ;
  - `TERMINE` n'est atteint que par la **clôture**, qui exige la date de fin, le kilométrage d'un
    véhicule et au moins une ligne de coût.
- **Sinistre** :
  - `DECLARE` → `DECLARE_ASSUREUR` → `EN_EXPERTISE` → `EN_REPARATION` → `CLOS`, ou
    `CLASSE_SANS_SUITE` ;
  - la clôture exige que les OT liés soient terminés ou annulés.

## API REST (`/api/v1/maintenance`)

| Ressource | Routes |
|---|---|
| Ordres de travail | CRUD, `/{id}/lignes`, `PATCH /{id}/statut`, `POST /{id}/cloture` |
| Plans | CRUD, `/echeances?horizonJours`, `/{id}/ordres-travail` |
| Sinistres | CRUD, `PATCH /{id}/statut`, `/{id}/couts`, `/{id}/ordres-travail`, `POST /{id}/reparations` |
| Prestataires | CRUD |
| Contrats d'assurance | CRUD, `/applicable?typeEngin&enginId&date` |
| Coûts | `/couts?debut&fin&typeEngin&enginId` |

Scores de santé : `/api/v1/scores-sante` et `/api/v1/scores-sante/dernier?vehiculeId`.

## API publique

`MaintenanceApi` :

| Méthode | Utilisée par |
|---|---|
| `ordresTravail`, `plansEntretien`, `echeances`, `sinistres`, `couts` | `ai` (agent, copilote) |
| `indisponibilites(debut, fin)` | `planning` |
| `dernierScoreSante`, `enregistrerScoreSante` | `ai` |

Événement publié : `EtatMaintenanceEnginModifieEvent`, à la clôture d'un OT et lors de la
déclaration, du changement d'immobilisation ou de la clôture d'un sinistre. Il est écouté par
`ai` pour recalculer le score de santé.

## Dépendances

`fleet`, par commandes : immobiliser, remettre en service, relever les compteurs.

## Effets de bord

| Action | Effet |
|---|---|
| OT immobilisant passé `EN_COURS` | Engin `EN_MAINTENANCE` |
| Sinistre déclaré immobilisant | Engin `IMMOBILISE` |
| Clôture d'OT | Compteurs relevés, dernière réalisation du plan mise à jour, remise en service si plus rien ne retient l'engin |
| Déclaration de sinistre | Contrat d'assurance et franchise repris automatiquement (contrat dédié prioritaire sur le contrat de flotte) |

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
