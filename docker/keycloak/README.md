# Keycloak local (LogiFlow)

Identité OIDC pour le frontend (`logiflow-frontend`) et validation JWT du backend (profil `dev`).

## Démarrage

Depuis `logiflow-backend/docker` :

```bash
docker compose up -d keycloak
```

- Console admin : http://localhost:8081/admin (`admin` / `change-me-local-only` par défaut)
- Issuer OIDC : http://localhost:8081/realms/logiflow

Le realm est rechargé depuis `import/logiflow-realm.json` à chaque démarrage (`start-dev`).

## Thème de connexion LogiFlow

Le thème Keycloak `logiflow` (dossier `themes/logiflow`) reprend le look de
`/connexion` Angular : canvas, carte blanche, pine, Fraunces / Geist.

Il est monté dans le conteneur (`./keycloak/themes`) et sélectionné via
`loginTheme: logiflow` sur le realm. PKCE + MFA restent inchangés.

Après modification CSS :

```bash
docker compose -f docker/docker-compose.yml up -d --force-recreate keycloak
bash docker/keycloak/reset-demo-passwords.sh
```

## Utilisateurs de test

Mot de passe commun local : **`demo`** (aligné sur les identités de démonstration).

| Utilisateur   | Rôle                      |
|---------------|---------------------------|
| `admin`       | ADMINISTRATEUR            |
| `responsable` | RESPONSABLE_EXPLOITATION  |
| `exploitant`  | EXPLOITANT                |
| `commercial`  | COMMERCIAL                |
| `atelier`     | ATELIER                   |
| `chauffeur`   | CHAUFFEUR                 |

Après un import frais, si la connexion échoue avec `invalid_user_credentials`,
réinitialiser les mots de passe :

```bash
bash docker/keycloak/reset-demo-passwords.sh
```

(Keycloak n'applique pas toujours les mots de passe en clair du JSON d'import.)

## MFA (recette)

L'action requise **Configure OTP** est activée dans le realm. Pour forcer l'OTP sur un utilisateur :
*Users → utilisateur → Required user actions → Configure OTP*.

Côté backend, activer `MFA_REQUIRED=true` pour refuser les jetons sans `amr` contenant `otp` (voir ADR 0002).

## Ré-export du realm

Après modification manuelle dans la console :

```bash
docker exec logiflow-keycloak /opt/keycloak/bin/kc.sh export \
  --realm logiflow --dir /tmp/export --users realm_file
docker cp logiflow-keycloak:/tmp/export/logiflow-realm.json ./keycloak/import/logiflow-realm.json
```

Ne committer que des mots de passe de test.

## Recette (10 scénarios)

Avec Keycloak sur 8081, backend profil `dev`, `pnpm start:keycloak` :

1. Ouvrir `/voyages` sans session → `/connexion` puis retour après login.
2. Login `exploitant` / `demo` → menu exploitation, `Bearer` sur `/api`.
3. Login `atelier` → `/commandes` manuel → `/403`.
4. Inactivité > 5 min → refresh silencieux.
5. Copilote : question streamée avec jeton.
6. Déconnexion → `/connexion`, session Keycloak terminée.
7. `MFA_REQUIRED=true` + OTP utilisateur → jeton avec `amr` contenant `otp`.
8. Révoquer session dans Keycloak → prochain appel → reconnexion.
9. Deux onglets : déconnexion dans l'un déconnecte l'autre.
10. `pnpm start` + backend `local` → mode démo inchangé.
