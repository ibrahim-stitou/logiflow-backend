# ADR 0004 — Copilote : base dédiée au service IA et accès aux données par outils via Spring

- **Statut** : accepté
- **Date** : 2026-09-23

## Contexte

Le bouton **Copilote** du header ouvrait une question/réponse unique : pas d'historique, pas de
streaming, et surtout **aucune donnée métier** (le service IA répondait avec les seules
connaissances du LLM, `sources` toujours vide). L'objectif est un vrai chatbot qui :

1. répond à partir des données du TMS (dossiers, voyages, flotte, maintenance, carburant…) **et**
   des connaissances du LLM ;
2. conserve des conversations (historique, reprise, avis) ;
3. respecte deux règles existantes (`docs/integration-ia.md`) : Angular ne parle qu'à Spring, et le
   service IA n'accède jamais à la base TMS.

## Décision

1. **Base propre au service IA.** Le service IA (`logiflow-ai-service`) possède sa base PostgreSQL
   `logiflow_ai` (schéma `copilote`, pgvector), créée dans le conteneur Postgres existant avec son
   propre rôle (`docker/postgres/init/02-ai-database.sql`) et migrée par Alembic. Elle stocke les
   conversations, messages, appels d'outils, avis et la base de connaissance vectorielle. Spring
   n'y a aucun accès ; le service IA n'a aucun accès à la base TMS.
2. **Accès aux données par tool-calling rappelé via Spring.** Le LLM choisit un outil ; le service
   IA demande à Spring de l'exécuter (`/internal/copilote/outils/**`). Les outils lisent les
   données via les `api` publiques des modules (règles Modulith inchangées) et renvoient des
   résultats compacts avec leurs **sources** (liens vers les fiches).
3. **Sécurité par jeton de contexte.** Pour chaque message, Spring émet un jeton opaque aléatoire,
   lié à l'utilisateur et à ses rôles, valable 5 minutes et révoqué en fin de réponse. Le service
   IA le présente à chaque appel d'outil avec une **clé de rappel** distincte de la clé Spring → IA.
   Les droits appliqués sont toujours ceux résolus par Spring (catalogue filtré par rôle, 403 hors
   droits), jamais ceux déclarés par le service IA ou choisis par le LLM.
4. **Streaming SSE de bout en bout** : LLM (SSE) → Flask (`text/event-stream`) → Spring
   (`SseEmitter`, relais événement par événement) → Angular (`fetch` + lecture du flux). Fermer la
   connexion interrompt la génération.
5. **LLM cloud compatible OpenAI**, configurable (`LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL`) :
   Groq `openai/gpt-oss-120b` par défaut. Embeddings optionnels (`EMBED_*`). Voir l'amendement.

## Alternatives écartées

- **Contexte pré-construit par Spring** (Spring devine l'intention, charge les données, les envoie
  avec la question) : pas d'appel retour, mais réponses moins pertinentes et logique d'intention
  dupliquée côté Spring.
- **Accès direct du service IA à la base TMS** (lecture seule) : contournerait les règles métier,
  les droits et le découpage modulaire.
- **Conversations stockées dans la base TMS** : couplerait le cycle de vie du service IA (schéma,
  migrations, volumétrie, embeddings) au monolithe.
- **SQLite pour le service IA** : pas de pgvector, peu adapté à plusieurs workers.

## Conséquences

- Le service IA devient **stateful** : sauvegarde de `logiflow_ai` à prévoir ; migrations Alembic
  appliquées au démarrage du conteneur.
- Communication **bidirectionnelle** Spring ↔ service IA sur le réseau interne ; les routes
  `/internal/copilote/**` ne doivent pas être exposées publiquement (reverse proxy).
- Les jetons de contexte sont en mémoire : en multi-instances, garantir l'affinité ou passer à un
  stockage partagé.
- Ajouter un outil = une classe `OutilCopilote` côté Spring (nom, description, JSON Schema, rôles) ;
  le service IA le découvre automatiquement via le catalogue.
- La qualité des réponses dépend du modèle local : `llama3.1:8b` gère correctement le tool-calling
  avec peu d'outils bien décrits ; un modèle plus grand améliorera la robustesse sans changement de
  contrat.

## Amendement (2026-09-24) — LLM cloud au lieu d'Ollama

Le premier test réel avec Ollama (`llama3.1:8b` sur un PC sans GPU, 16 Go de RAM) a donné 2 à
8 minutes par réponse : ~13 tokens/s en lecture de prompt (~3 000 tokens avec les outils),
~3 tokens/s en génération, et 6 Go de RAM occupés. LogiFlow étant un projet d'apprentissage et de
démonstration, et non de production, Ollama est **retiré** au profit d'un fournisseur cloud au
format OpenAI, **gratuit** (Groq par défaut ; Gemini, Mistral, OpenRouter possibles sans changer
de code).

- Conséquence acceptée : les données consultées par les outils (références, immatriculations,
  statuts) sont envoyées au fournisseur. Acceptable pour des données de démonstration uniquement.
- Les paliers gratuits imposent des quotas : un dépassement (HTTP 429) devient l'événement
  `erreur` de code `QUOTA_LLM`.
- La base de connaissance devient optionnelle (Groq ne fournit pas d'embeddings) et la dimension
  des vecteurs n'est plus figée (migration Alembic 0002).
