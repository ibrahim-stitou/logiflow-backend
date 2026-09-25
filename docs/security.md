# Sécurité de LogiFlow TMS

Ce document décrit le socle de sécurité du backend LogiFlow TMS : le modèle d'authentification et
d'autorisation, le rôle exact de Spring Security, le câblage de l'authentification multi-facteurs
(MFA) via le fournisseur d'identité (Keycloak), ainsi que la configuration associée.

## 1. Vue d'ensemble

Le backend LogiFlow est un **resource server OAuth2/OIDC** : il **valide** des jetons JWT émis par
un fournisseur d'identité externe (Keycloak) et **n'émettra jamais lui-même** de jetons, de mots de
passe ou de codes MFA. Les grandes lignes du modèle :

| Aspect               | Choix                                                              | Motivation                                                      |
|----------------------|--------------------------------------------------------------------|------------------------------------------------------------------|
| Authentification     | Déléguée à Keycloak (flux OAuth2 + PKCE côté frontend Angular)     | IdP spécialisé : mots de passe, MFA, réinitialisation, SSO       |
| Autorisation         | Claims JWT (`roles`, `realm_access.roles` Keycloak) → rôles Spring | Zéro appel réseau, vérifiée à chaque requête (stateless)          |
| État de session      | Aucun (`SessionCreationPolicy.STATELESS`)                          | Scalable ; le JWT est la seule source de vérité                  |
| Réseau de sécurité   | Deny-by-default : seul un périmètre explicite est public           | Réduit la surface d'attaque                                      |
| Second facteur (MFA) | Exigé par le backend **sur les jetons** (claims `amr`/`acr`)       | Voir la partie 5 ci-dessous                                       |

Flux nominal :

```
Angular ──(login + mot de passe)──▶ Keycloak
Angular ◀─(défi OTP / second facteur)── Keycloak
Angular ──(code OTP)──▶ Keycloak
Angular ◀─────(Access Token JWT + amr=["pwd","otp"])──── Keycloak
Angular ──(GET /api/... + Authorization: Bearer <jwt>)──▶ Backend
Backend: 1) signature + exp + issuer  2) preuve MFA (amr/acr)  3) rôles
Backend ◀──────────────────(200/401/403)──────────────── Angular
```

Le frontend Angular est le client OAuth2 (Authorization Code + PKCE). Le backend ne connaît que le
token, et le **rejette** s'il ne prouve pas une MFA, ce qui force le frontend à renvoyer
l'utilisateur vers la ré-authentification avec second facteur.

## 2. Le socle Spring Security

La classe unique à connaître est `com.logiflow.tms.config.SecurityConfig`. Elle construit la
`SecurityFilterChain` :

1. **CORS** : source configurée par `logiflow.cors.allowed-origins` (voir partie 7).
2. **CSRF désactivé** : l'API ne repose que sur le bearer token (pas de cookies de session), la
   protection CSRF classique est donc sans objet.
3. **Sessions stateless** : aucun `JSESSIONID`, le token porte tout le contexte.
4. **Deny-by-default** :

   ```java
   authorize
     .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
     .anyRequest().authenticated()
   ```

   Endpoints publics (uniquement) :

   ```
   /actuator/health, /actuator/health/**, /actuator/info
   /v3/api-docs, /v3/api-docs/**
   /swagger-ui.html, /swagger-ui/**
   ```

   Tout le reste (tous les `*Controller` métier) exige un JWT valide.
5. **Resource server JWT** : `oauth2ResourceServer().jwt(...)` branché sur le `JwtDecoder`
   auto-configuré (signature, expiration... vérifiées de façon standard).
6. **En-têtes de sécurité HTTP** :
   - `X-Content-Type-Options: nosniff` ;
   - `X-Frame-Options: DENY` ;
   - `Referrer-Policy: no-referrer`.

### 2.1 Rôles et autorisations

Le `JwtAuthenticationConverter` de `SecurityConfig` transforme les claims en `GrantedAuthority`
Spring prefixed `ROLE_` :

- le claim **`roles`** (format applicatif) ;
- le claim **`realm_access.roles`** (format Keycloak), ajouté en complément.

L'accès aux ressources se contrôle ensuite avec `hasRole("...")` / `@PreAuthorize` par route. Le
module `iam` définit les rôles applicatifs (`RoleUtilisateur` : `ADMINISTRATEUR`, `RESPONSABLE_EXPLOITATION`,
`EXPLOITANT`, `COMMERCIAL`, `ATELIER`, `CHAUFFEUR`).

### 2.2 Mode permissif local (`logiflow.security.permissive-local-profile`)

En profil `local`/`test`, **aucun IdP n'est configuré**. Quand `permissive-local-profile=true`,
`SecurityConfig` **n'appelle pas du tout** `oauth2ResourceServer()` (sinon Spring exigerait un
`JwtDecoder` au démarrage) : les routes protégées sont alors inaccessibles sans authentification --
les tests d'intégration injectent l'authentification via
`SecurityMockMvcRequestPostProcessors.jwt()`.

> ⚠️ Ce mode est exclusivement réservé au développement local : `dev`/`prod` le désactivent
> explicitement (`false`). Ne jamais activer ailleurs.

## 3. Utilisateur courant et audit

`com.logiflow.tms.shared.infrastructure.security.SecurityContextService` expose l'utilisateur
courant depuis le JWT (`sub`, `preferred_username`, rôles) et fournit
`identifiantPourAudit()` utilisé par l'audit applicatif (colonnes `created_by`/`updated_by`). Si
le contexte est vide (batch, tâche planifiée), l'identifiant retombe sur `system`.

## 4. Gestion des erreurs de sécurité

Toutes les réponses d'erreur utilisent le format **RFC 7807** (`ProblemDetail`) via
`GlobalExceptionHandler` / `ApiError`, avec un identifiant de corrélation (`X-Correlation-Id`)
permettant de retrouver la trace complète dans les logs.

| Situation                                   | Statut | Déclencheur                                                    |
|---------------------------------------------|--------|-----------------------------------------------------------------|
| Aucun token / token invalide ou expiré      | **401** | `BearerTokenAuthenticationEntryPoint` du resource server        |
| Token valide mais sans preuve MFA requise   | **401** | `MfaJwtValidator` → erreur OAuth2 `mfa_required`                |
| Token valide mais droits insuffisants       | **403** | `AccessDeniedHandler` / `AccessDeniedException`                 |
| Erreur applicative non anticipée            | **500** | `GlobalExceptionHandler` (détail technique jamais exposé)       |

## 5. MFA (second facteur)

### 5.1 Principe

Le MFA est **entièrement délégué à Keycloak** : l'IdP n'émet aucun jeton tant que le second
facteur n'a pas été validé. Le backend refuse à son tour **tout jeton qui ne prouve pas une étape
MFA**, en contrôlant les claims OIDC standard pendant la validation du jeton :

- **`amr`** (*Authentication Methods Reference*) : liste des méthodes réellement utilisées.
  Keycloak pose par exemple `["pwd", "otp"]` quand le mot de passe puis un OTP ont été saisis.
- **`acr`** (*Authentication Context Class Reference*) : niveau d'authentification (complément
  optionnel, ex. `phr`, `loa3`).

Aucune donnée MFA (secret, code, session partielle) n'est stockée en base : le backend ne devient
pas un second gestionnaire d'identité. La décision complète est documentée dans
[docs/adr/0002-connexion-mfa-keycloak.md](adr/0002-connexion-mfa-keycloak.md).

### 5.2 Implémentation

- `com.logiflow.tms.config.MfaJwtValidator` implémente `OAuth2TokenValidator<Jwt>`.
- Le bean est **auto-découvert** par la chaîne de validation du `JwtDecoder` auto-configuré de
  Spring Boot 4 (le module `spring-boot-security-oauth2-resource-server` collecte tous les beans
  `OAuth2TokenValidator<Jwt>` via `ObjectProvider` et les chaîne derrière les validations par
  défaut — timestamp, issuer). Aucun `JwtDecoder` custom n'est écrit.
- Inactif par défaut ; activé par `logiflow.security.mfa.required=true`.
- Règles du validateur (activé) :
  1. Le claim `amr` (liste ou valeur scalaire) **contient une méthode** de la liste configurée
     `mfa.amr-methods` → accepté ;
  2. sinon, si des valeurs `mfa.acr-values` sont configurées, le claim `acr` en contient une →
     accepté ;
  3. sinon → jeton rejeté avec l'erreur OAuth2 `mfa_required` (**401**), le frontend renvoie
     l'utilisateur vers la ré-authentification Keycloak avec le second facteur.

### 5.3 Configuration Keycloak (exemple)

1. Dans le realm, activer le second facteur (authentificateur **OTP/TOTP**) dans le flow *Browser*,
   ou comme *Required Action* `Configure OTP`.
2. Vérifier que le claim `amr` est bien émis dans l'Access Token (défaut Keycloak pour OTP/WebAuthn ;
   sinon utiliser `acr-values` avec un flow conditionnel dédié).
3. Côté backend, exiger la preuve :

   ```env
   MFA_REQUIRED=true
   MFA_AMR_METHODS=mfa,otp,totp,webauthn
   MFA_ACR_VALUES=
   ```

4. Tester :
   - jeton **sans** second facteur → le backend répond **401** ;
   - jeton **avec** `amr=["pwd","otp"]` → le backend accepte.

Le test d'intégration `MfaJwtResourceServerIT` reproduit exactement ce scénario en local (JwtDecoder
réel avec clé RSA, jeton signé, profil non permissif).

## 6. Configuration (propriétés)

### 6.1 OAuth2 resource server

`spring.security.oauth2.resourceserver.jwt.*` est renseigné par les profils réalistes (`dev`,
`prod`) via variables d'environnement (voir `.env.example`) :

```env
OAUTH2_ISSUER_URI=https://keycloak.exemple/realms/logiflow
OAUTH2_JWK_SET_URI=https://keycloak.exemple/realms/logiflow/protocol/openid-connect/certs
```

- `issuer-uri` : validation de l'émetteur ;
- `jwk-set-uri` : clés publiques de vérification de signature.

> ⚠️ Aucune valeur de repli dans `dev` : l'application ne démarre pas sans IdP réel. Les profils
> `local`/`test` laissent ces propriétés absentes et tournent en mode permissif (partie 2.2).

### 6.2 MFA

Sous préfixe `logiflow.security.mfa` :

| Propriété           | Variable d'env | Défaut                           | Rôle                                   |
|---------------------|----------------|-----------------------------------|-----------------------------------------|
| `required`          | `MFA_REQUIRED` | `false`                           | Exige la preuve MFA sur chaque jeton    |
| `amr-methods`       | `MFA_AMR_METHODS` | `mfa,otp,totp,webauthn`        | Méthodes `amr` acceptées comme preuve   |
| `acr-values`        | `MFA_ACR_VALUES`  | vide (contrôle `amr` seul)      | Valeurs `acr` acceptées en complément   |

### 6.3 CORS

`logiflow.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`, défaut `http://localhost:4200`) ;
méthodes `GET, POST, PUT, PATCH, DELETE, OPTIONS` ; en-têtes autorisés `Authorization`,
`Content-Type`, `X-Correlation-Id` ; en-tête exposé `X-Correlation-Id` ; `allowCredentials=true`.

## 7. Mode Keycloak local (frontend + profil `dev`)

Stack minimale pour une connexion réelle en développement :

1. `make keycloak` (ou `docker compose … up -d keycloak`) — realm `logiflow` sur le port **8081**.
2. Backend avec le profil **`dev`** (JWT obligatoire, pas d'utilisateur fictif) :

   ```bash
   export OAUTH2_ISSUER_URI=http://localhost:8081/realms/logiflow
   export OAUTH2_JWK_SET_URI=http://localhost:8081/realms/logiflow/protocol/openid-connect/certs
   export CORS_ALLOWED_ORIGINS=http://localhost:4200
   export MFA_REQUIRED=false
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. Frontend Angular : `pnpm start:keycloak` (client `logiflow-frontend`, PKCE, jeton sur `/api/`).

Le profil **`local`** reste disponible pour le mode démo frontend (`pnpm start`) sans IdP.

Vérification rapide (après connexion dans le navigateur, copier l'access token) :

```bash
curl -i -H "Authorization: Bearer <access_token>" http://localhost:8080/api/v1/voyages
```

Sans en-tête `Authorization`, la même URL renvoie **401**.

## 8. Points de renforcement recommandés (non implémentés)

Sont laissés volontairement à la charge de l'IdP et de l'infrastructure, hors périmètre du backend :

- **Rate limiting / anti-bruteforce** sur le login et le second facteur → côté Keycloak (ou gateway) ;
- **Rotation des clés JWK** et révocation des tokens → gestion du realm ;
- **Scopes granulaires** : les rôles actuels sont globaux au realm ; des scopes par ressource
  pourraient être ajoutés si l'API s'ouvre à des clients non frontend ;
- **Audit des connexions** : les événements de login/MFA sont déjà journalisés par Keycloak ;
- **Objectifs SLA de sécurité** à inscrire si besoin dans une politique de sécurité du projet.

## 9. Références

- [docs/adr/0002-connexion-mfa-keycloak.md](adr/0002-connexion-mfa-keycloak.md) — décision MFA
- [docs/architecture.md](architecture.md) — place du module `iam` (aucune dépendance sortante)
- Code : `config/SecurityConfig.java`, `config/MfaJwtValidator.java`,
  `shared/infrastructure/security/SecurityContextService.java`,
  `shared/infrastructure/web/GlobalExceptionHandler.java`,
  `iam/` (modèle `Utilisateur`, rôles)