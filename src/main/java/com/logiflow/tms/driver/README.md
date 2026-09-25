# Module `driver` — chauffeurs

## Rôle

Chauffeurs, qualifications et capacité à être affectés à un voyage.

## Modèle

- **`Chauffeur`** :
  - matricule, identité, contrat (`CDI`, `CDD`, `INTERIM`) ;
  - statut (`ACTIF`, `INACTIF`) et disponibilité (`DISPONIBLE`, `EN_VOYAGE`, `EN_REPOS`,
    `EN_CONGE`).
- **`ProfilChauffeur`** :
  - catégories de permis, habilitations datées (ADR, FIMO/FCO…), passeport ;
  - site de rattachement, téléphone.
- **Solde de temps de conduite** (minutes), utilisé par la conformité et par l'agent de
  planification.
- **`ExigencesAffectation`** : ce qu'un voyage exige d'un chauffeur (permis, ADR, passeport).

## API REST

- `/api/v1/chauffeurs` : CRUD et recherche.
- `/{id}/disponibilite` et `/{id}/statut` : changements de disponibilité et de statut.

## API publique

`ChauffeurApi` : consultation, `listerPourPlanification` (chauffeurs affectables avec motif de
non-affectation), vérification des exigences. Utilisée par `planning` et `ai`.

## Dépendances

`document` : permis, carte conducteur, visite médicale, pièce d'identité, passeport, visa.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
