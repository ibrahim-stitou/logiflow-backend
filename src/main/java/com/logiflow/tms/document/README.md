# Module `document` — pièces jointes

## Rôle

Stockage des **pièces jointes** de toutes les entités, avec leur type et leur date
d'expiration : c'est la base du contrôle de validité des documents réglementaires.

## Modèle

- **`Document`** : entité rattachée (type + id), type de document, référence, fichier (URL de
  stockage), date d'expiration.
- **`TypeEntiteDocumentable`** : `VEHICULE`, `REMORQUE`, `CHAUFFEUR`, `PRISE_CARBURANT`,
  `ORDRE_TRAVAIL`, `SINISTRE`, `CONTRAT_ASSURANCE`, `PRESTATAIRE`.
- **`TypeDocument`** :
  - véhicule : carte grise, assurance, contrôle technique, ADR, photo ;
  - chauffeur : permis, carte conducteur, FIMO/FCO, visite médicale, pièce d'identité,
    passeport, visa ;
  - carburant : justificatif ;
  - maintenance : devis, facture, rapport d'intervention, constat amiable, rapport de police,
    rapport d'expertise, déclaration de sinistre, attestation d'assurance, conditions du contrat.

## API REST

`/api/v1/documents`, en `multipart` à l'envoi, avec filtre `typeEntite` et `entiteId`.

## API publique

- `DocumentApi` : lister les documents d'une entité ; `tousValides(typeEntite, id, date)`.
- Événement `DocumentEntiteModificationEvent`, publié à chaque ajout ou suppression. Il est
  écouté par `carburant` et par `ai` (réanalyse de maintenance).

## Stockage

Disque local (`STORAGE_LOCAL_PATH`), via le port `FileStorageService`. Un adaptateur S3 pourra le
remplacer.

## Dépendances

Aucune. Ce module est utilisé par `fleet`, `driver`, `carburant` et `ai`.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
