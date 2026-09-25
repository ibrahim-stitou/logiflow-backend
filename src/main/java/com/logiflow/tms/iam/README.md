# Module `iam` — utilisateurs et rôles

## Rôle

Gère les **utilisateurs applicatifs** et leurs **rôles**. L'authentification elle-même est
déléguée à Keycloak (voir [docs/security.md](../../../../../../../docs/security.md)) : ce module
ne stocke aucun mot de passe.

## Modèle

- `Utilisateur` : identifiant, login, nom, e-mail, rôles, actif.
- `RoleUtilisateur` :
  - `ADMINISTRATEUR` : tout ;
  - `RESPONSABLE_EXPLOITATION` et `EXPLOITANT` : exploitation ;
  - `COMMERCIAL` : clients, commandes, dossiers ;
  - `ATELIER` : flotte, maintenance, carburant ;
  - `CHAUFFEUR` : ses voyages, ses prises de carburant.

## API REST

`/api/v1/utilisateurs` (CRUD).

## API publique

`UtilisateurApi`.

## Dépendances

Aucune. Les autres modules lisent l'utilisateur courant via `shared.infrastructure.security`,
jamais via `iam`.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
