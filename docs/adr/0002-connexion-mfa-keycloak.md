# ADR 0002 — Connexion avec MFA déléguée au fournisseur d'identité (Keycloak)

- **Statut** : Acceptée
- **Date** : 2026-09-12
- **Décideurs** : Équipe LogiFlow

## Contexte

LogiFlow requiert une connexion avec **authentification multi-facteurs (MFA)**. Le backend est un
*resource server* OAuth2/JWT : il ne gère ni formulaires de login, ni mots de passe, ni sessions.
L'authentification (mot de passe puis second facteur) est déléguée à un fournisseur d'identité
externe (Keycloak) via le flux OIDC, le frontend Angular étant le client OAuth2 (Authorization Code
+ PKCE). Le backend ne reçoit qu'un jeton JWT à valider.

L'enjeu est donc : **comment garantir qu'un requérant a réellement passé l'étape MFA côté IdP**,
sans faire du backend un second point de gestion d'identité (stockage de codes OTP, de secrets, de
sessions …), ce qui dupliquerait la logique du fournisseur d'identité et exposerait le backend.

## Décision

Le MFA est configuré **entièrement côté Keycloak** (l'IdP n'émet aucun jeton tant que le second
facteur n'est pas validé). Le backend, lui, **refuse tout jeton JWT qui ne prouve pas qu'une étape
MFA a été réalisée**, en contrôlant les claims OIDC standard du jeton pendant sa validation :

- **`amr`** (Authentication Methods Reference) : liste des méthodes d'authentification réellement
  employées. Keycloak expose p. ex. `["pwd", "otp"]` quand l'utilisateur a saisi son mot de passe
  puis validé un OTP.
- **`acr`** (Authentication Context Class Reference) : niveau de contexte d'authentification, utilisé
  en complément si des valeurs acceptées sont configurées (ex. `phr`, `loa3`).

Mécanisme d'application :

- Le bean `MfaJwtValidator` (implémentation de `OAuth2TokenValidator<Jwt>`) est auto-découvert par
  la chaîne de validation du `JwtDecoder` auto-configuré de Spring Boot 4 (`objectProvider` de
  `OAuth2TokenValidator`), sans écrire de `JwtDecoder` custom : le contrôle de l'issuer, de la
  signature et de l'expiration reste géré par Spring Boot.
- Inactif par défaut ; activé par `logiflow.security.mfa.required=true` (env : `MFA_REQUIRED`).
- L'échec du contrôle renvoie une erreur OAuth2 `mfa_required` → réponse **401** : le frontend
  déclenche alors un `logout` ou une ré-authentification avec second facteur côté Keycloak.
- Aucune donnée MFA n'est stockée en base : aucun changement de schéma, aucun module métier impacté.

## Conséquences

**Positives**

- Zéro stockage de secrets/codes côté backend : la base de données et le périmètre d'attaque du
  backend ne s'étendent pas.
- Mécanisme générique (claims normalisés OIDC) : compatible avec tout IdP qui pose `amr`/`acr`
  (Keycloak notamment).
- Branché sur l'auto-configuration de Spring Boot : pas de réécriture du décodage JWT,
  rétrocompatibilité avec les profils `local`/`test` (le validateur est inactif en mode permissif).

**Négatives / compromis assumés**

- Le backend ne peut pas forcer *lui-même* un utilisateur non MFA à s'inscrire : Keycloak doit
  exiger le second facteur (voir la configuration ci-dessous), sinon les jetons émis ne porteront
  jamais de preuve MFA et le backend rejettera ces utilisateurs en 401.
- La distinction "preuve MFA" repose sur le contenu des claims, dont la précision dépend de la
  configuration du realm Keycloak (il faut activer les Required Actions / flows adéquats).
- Un admin Keycloak reste la seule voie de réinitialisation du second facteur.

## Configuration Keycloak type

1. Dans le realm, activer le second facteur pour les utilisateurs concernés : authentificateur
   **OTP** (TOTP) dans le flow *Browser*, ou *Required Action* `Configure OTP`.
2. Vérifier que le **claim `amr`** est bien émis : il l'est par défaut pour l'OTP/webauthn dans
   Keycloak récent ; sinon utiliser `acr-values` (niveau d'authentification) en configurant un flow
   d'authentification conditionnel avec un `acr` dédié.
3. Côté backend (`env`), activer l'exigence :

   ```env
   MFA_REQUIRED=true
   MFA_AMR_METHODS=mfa,otp,totp,webauthn
   MFA_ACR_VALUES=
   ```

4. Tester avec un token issu sans second facteur (doit être refusé en 401) puis avec le second
   facteur (accepté).

## Alternatives écartées

- **Login + OTP gérés par le backend** (mots de passe, envoi de codes par email/SMS, validation et
  émission de jetons côté applicatif) : duplique la gestion d'identité de Keycloak dans le backend,
  alors que le cahier des charges conserve un IdP externe ; ajoute du stockage de secrets, des points
  de réinitialisation et un périmètre d'attaque supplémentaire.
- **Multi-modalités côté backend** (TOTP en plus de l'OTP email) : aucun intérêt tant que
  l'authentification reste déléguée à l'IdP ; tous les facteurs sont gérés par Keycloak.