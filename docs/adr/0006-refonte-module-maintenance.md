# ADR 0006 — Refonte du module maintenance (OT détaillés, sinistres, assurance, coûts)

- **Statut** : accepté
- **Date** : 2026-09-25

## Contexte

Le module `maintenance` ne permettait pas de gérer une flotte ni de nourrir l'agent de
maintenance prédictive :

- l'ordre de travail se limitait à un véhicule, un type, une date, une durée et un montant (pas
  de kilométrage, de lignes de coût, de prestataire ni de documents, et seulement 4 statuts) ;
- le plan d'entretien ne mémorisait pas sa dernière réalisation, et l'agent devait deviner
  l'échéance (kilométrage modulo la périodicité) ;
- les sinistres, l'assurance, les prestataires et le suivi des coûts n'existaient pas ;
- les remorques n'étaient pas couvertes ;
- un OT n'agissait ni sur le statut de la flotte ni sur la disponibilité en planification.

## Décision

1. **Engins.** Tous les agrégats portent un `EnginRef` (VEHICULE ou REMORQUE). Les lectures de
   flotte passent par `VehiculeApi` et `RemorqueApi`.
2. **Ordre de travail détaillé.**
   - Référence `OT-AAAA-nnnnnn`, type, nature, priorité, origine (manuelle, plan, sinistre,
     agent IA, panne), prestataire, dates planifiées et réelles.
   - Lignes de coût (main-d'œuvre, pièces, sous-traitance…) avec TVA, budget estimé distinct du
     réel, facture.
   - Workflow `PLANIFIE → EN_COURS ⇄ EN_ATTENTE_PIECES → TERMINE`, et `ANNULE` depuis un statut
     ouvert.
   - La fin ne passe que par la **clôture**, qui exige la date de fin réelle, le kilométrage (pour
     un véhicule) et au moins une ligne de coût.
3. **Plan d'entretien.**
   - Périodicités en km, mois et heures, et seuils d'alerte.
   - La **dernière réalisation** (date, km, heures) est mise à jour par la clôture des OT liés.
   - Le domaine calcule la prochaine échéance (`OK`, `ALERTE`, `ECHU`) avec un usage mesuré sur
     les OT.
4. **Sinistres, prestataires, contrats d'assurance.**
   - Le sinistre porte les circonstances, le tiers, le suivi assurance (dossier, expertise,
     franchise, indemnité) et un workflow jusqu'à `CLOS`.
   - Le contrat applicable, dédié ou à défaut de flotte, et sa franchise sont repris
     automatiquement.
   - Le coût net d'un sinistre = réparations liées − indemnité.
   - Un sinistre ne se clôt qu'une fois ses OT terminés ou annulés.
5. **Intégrations par l'API publique, sans cycle Modulith.**
   - Maintenance → flotte, par **commandes** de `VehiculeApi` et `RemorqueApi` (immobilisation,
     remise en service, relevé de compteurs) plutôt que par événements, qui créeraient un cycle.
   - Planning → maintenance, via `MaintenanceApi.indisponibilites` : un engin retenu à l'atelier
     sur la période est refusé ou n'est pas proposé.
   - Documents : nouveaux types d'entité (OT, sinistre, contrat, prestataire) et de pièces (devis,
     facture, constat, rapport d'expertise…).
6. **Coûts.** `GET /maintenance/couts` (et `MaintenanceApi.couts`) : totaux HT/TTC, écart au
   budget, répartitions par type, nature, mois et engin, et sinistralité.
7. **Schéma recréé** (`V31`) et nouveau jeu de démonstration (`seed/V32`).

## Écarts assumés

- **Colonnes JSON plutôt que tables filles.** Les lignes de coût et les engins couverts d'un
  contrat sont stockés en JSON : ils n'ont pas de vie propre.
- **Pas de coût au kilomètre.** Les kilomètres parcourus sur la période relèvent de `planning`,
  et `maintenance` ne peut pas en dépendre (cycle).
- **Score de santé inchangé.** La colonne `vehicule_id` porte désormais l'identifiant de l'engin.

## Conséquences

- **Agent de maintenance prédictive.**
  - Il reçoit l'échéance calculée par le module et n'estime plus que sa projection dans le
    temps, avec l'usage réel et les voyages planifiés.
  - Il analyse aussi les remorques et tient compte des sinistres et des OT ouverts.
  - Sa proposition « Planifier l'OT » ouvre un OT d'origine `AGENT_IA` sur le créneau proposé.
  - Il ne dépend plus d'un clic : une analyse de la flotte tourne chaque nuit, et chaque engin est
    réanalysé après la clôture d'un OT, un sinistre ou une modification de ses documents
    (`EtatMaintenanceEnginModifieEvent`).
- **Copilote.** `consulter_maintenance` couvre les remorques ; deux outils s'ajoutent,
  `rechercher_sinistres` et `couts_maintenance`.
- **Frontend.** Le module est rebâti : tableau de bord, OT, plans, sinistres, coûts,
  prestataires et assurances, plus une section maintenance sur les fiches véhicule et remorque.
- **Tests d'intégration.** Les anciens endpoints `/api/v1/ordres-travail` et
  `/api/v1/plans-entretien` sont remplacés par `/api/v1/maintenance/**`. Les seeds sont chargés
  en test (Flyway parcourt `db/migration` récursivement) : un test d'intégration ne doit donc pas
  supposer une base vide.
