# Intégration avec le service IA (Flask)

## Principe

Les agents IA (groupage, maintenance prédictive, copilote conversationnel) sont développés dans
une **application Flask séparée**, déployée et versionnée indépendamment de ce backend.

**Règle non négociable : Angular ne parle jamais à Flask directement.** Le service Flask n'est
jamais exposé publiquement — il vit sur un réseau interne, accessible uniquement depuis le backend
Spring Boot. Toutes les API consommées par le frontend restent exposées par Spring Boot, qui joue
le rôle de façade (BFF) : il authentifie et autorise l'utilisateur, assemble le contexte métier
nécessaire (en interrogeant ses propres modules via leurs `api` publiques), appelle Flask en
interne, puis renvoie une réponse déjà mise en forme au frontend.

```mermaid
graph LR
    NG[Angular] -->|HTTPS, JWT utilisateur| SB[Spring Boot<br/>façade API]
    SB -->|réseau interne uniquement<br/>clé API de service| FL[Flask<br/>agents IA]
    SB --> PG[(PostgreSQL)]
    FL -.->|jamais d'accès direct DB<br/>ni d'appel entrant| NG
```

Conséquences de ce principe :

- Flask n'a **aucun accès direct** à la base de données PostgreSQL du TMS : tout ce dont il a
  besoin lui est transmis dans la requête HTTP par Spring Boot.
- Flask n'a **aucune connaissance** du JWT utilisateur ni de Keycloak : l'authentification
  utilisateur s'arrête à Spring Boot. Entre Spring Boot et Flask, l'authentification est un
  **secret partagé de service à service** (voir ci-dessous), pas une identité utilisateur.
- Si Flask est indisponible, lent ou renvoie une erreur, l'expérience se dégrade **sans jamais
  casser** les fonctionnalités déterministes déjà implémentées (ex. le filtrage dur du groupage
  dans `dossier.domain.service.DossierDomainService` continue de fonctionner sans IA).

## Authentification service à service

Un en-tête `X-Internal-Api-Key` porte une clé statique partagée, distincte par environnement,
jamais committée (fournie via variable d'environnement `AI_SERVICE_API_KEY`). C'est un choix
pragmatique pour un projet de 8 semaines ; en production, on lui préférerait un flux OAuth2
client-credentials ou du mTLS entre les deux services — voir "Points à vérifier".

## Configuration (Spring Boot)

| Variable d'environnement | Propriété Spring | Rôle |
|---|---|---|
| `AI_SERVICE_BASE_URL` | `logiflow.ai-service.base-url` | URL racine du service Flask (ex. `http://ai-service:8000`) |
| `AI_SERVICE_API_KEY` | `logiflow.ai-service.api-key` | Clé partagée envoyée dans `X-Internal-Api-Key` |
| `AI_SERVICE_CONNECT_TIMEOUT_MS` | `logiflow.ai-service.connect-timeout` | Timeout de connexion (défaut 2 s) |
| `AI_SERVICE_READ_TIMEOUT_MS` | `logiflow.ai-service.read-timeout` | Timeout de lecture (défaut 8 s) |

## Contrat d'API interne (Flask)

Toutes les routes sont préfixées `/internal/ai/v1` côté Flask, pour bien les distinguer d'une
éventuelle API publique future. Elles ne sont **jamais** appelées depuis Angular.

### 1. Copilote conversationnel — *implémenté*

`POST /internal/ai/v1/copilot/ask`

Requête (envoyée par Spring Boot) :

```json
{
  "question": "Quels camions sont libres demain pour Madrid ?",
  "utilisateur": { "id": "auth0|abc123", "roles": ["EXPLOITANT"] },
  "correlationId": "5e1b3c1a-..."
}
```

Réponse attendue (200) :

```json
{
  "reponse": "3 véhicules sont disponibles demain : ...",
  "sources": ["vehicule:AB-123-CD", "voyage:VOY-2026-000042"],
  "confiance": 0.82
}
```

Exposé au frontend par Spring Boot via `POST /api/v1/ia/copilote/questions`. Si Flask est
indisponible, l'endpoint renvoie **503** (RFC 7807) — il n'existe pas de repli déterministe
pertinent pour une question ouverte en langage naturel.

### 2. Agent de groupage — *implémenté (version simplifiée)*

`POST /internal/ai/v1/groupage/analyser`

Requête :

```json
{
  "dossiers": [
    { "id": "...", "reference": "DT-2026-000123", "poidsBrutKg": 500, "volumeM3": 2.5, "nbPalettes": 10, "contientAdr": false }
  ],
  "correlationId": "..."
}
```

Réponse attendue (200) :

```json
{
  "propositions": [
    {
      "dossierIds": ["...", "..."],
      "score": 0.87,
      "confiance": 0.9,
      "gainKm": 120.5,
      "gainMarge": 340.0,
      "justification": "Points de chargement à 4 km, fenêtres compatibles, remplissage 92 %."
    }
  ]
}
```

Exposé au frontend par Spring Boot via `POST /api/v1/ia/groupage/propositions`.

**Dégradation gracieuse** : la version actuelle transmet à Flask les dossiers déjà filtrés par
les règles dures (`DossierTransport.estGroupableAvec`, voir module `dossier`). Si Flask est
indisponible, `GroupageAdvisorService` intercepte l'échec et renvoie directement les paires
compatibles selon les règles dures, avec un score neutre et une justification indiquant que
l'enrichissement IA était indisponible — **le flux métier n'est jamais bloqué**.

**Limite assumée de cette version** : le contrat n'inclut pas encore les coordonnées
géographiques des sites de chargement/déchargement (nécessaires au calcul de proximité réel).
Cela suppose d'enrichir `dossier.api.DossierSummary` avec les identifiants de sites, puis de les
résoudre via `referential.api.SiteApi`. Prévu dans un lot ultérieur, une fois l'algorithme de
scoring géographique prêt côté Flask.

### 3. Agent de maintenance prédictive — *contrat documenté, non implémenté*

`POST /internal/ai/v1/maintenance/recommander` — à faire une fois le besoin précisé côté Flask.
Reprendrait le même schéma : Spring Boot assemble les données (`ScoreSante`, historique
`OrdreTravail` via le module `maintenance`), les transmet à Flask, reçoit une recommandation
hiérarchisée, la journalise et la renvoie au frontend.

## Journalisation des interactions

Chaque appel à Flask est journalisé en base par le module `ai`
(`ai.infrastructure.persistence.entity.InteractionIaEntity`) : type d'interaction, requête
envoyée (résumé), succès/échec, durée, et pour le copilote, la question posée et l'utilisateur à
l'origine — condition nécessaire à l'évaluation de la qualité des agents (taux d'adoption,
précision/rappel) décrite dans le CDC.

## Environnements

- **Local** : le service Flask tourne dans son propre dépôt/conteneur. Un bloc `ai-service`
  commenté est prévu dans `docker/docker-compose.yml`, à décommenter et pointer vers l'image du
  dépôt Flask une fois disponible. Tant qu'il n'est pas démarré, Spring Boot fonctionne
  normalement : seuls les endpoints `/api/v1/ia/**` sont affectés (503 pour le copilote, repli
  déterministe pour le groupage).
- **CI** : aucun appel réseau réel vers Flask dans les tests — `AiServiceClientPort` est mocké
  dans les tests applicatifs, et les tests d'intégration pointent volontairement vers une URL
  injoignable pour exercer le chemin de dégradation.

## Points à vérifier

- Authentification service-à-service par clé statique : à remplacer par OAuth2
  client-credentials ou mTLS avant toute mise en production réelle.
- Le contrat exact des réponses Flask (noms de champs, formats) est une proposition côté Spring
  Boot : à valider avec l'équipe qui implémente l'application Flask, et à ajuster dans
  `ai.infrastructure.client.dto.*` en conséquence.
- `RestClient` (Spring Framework 7) est utilisé pour l'appel HTTP synchrone : à confirmer que
  l'API n'a pas changé par rapport aux versions Boot 3.2+ où elle a été introduite.
