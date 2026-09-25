# Module `ai` — façade des agents IA

## Rôle

Seul point de contact entre le TMS et le service IA Flask (`logiflow-ai-service`). Pour chaque
agent, le module :

- assemble le contexte métier via les `api` des autres modules ;
- appelle Flask ;
- revalide ou enregistre le résultat ;
- journalise l'interaction.

Il expose aussi les **outils** que le copilote appelle.

Documentation complète : [docs/agents-ia.md](../../../../../../../docs/agents-ia.md)
(fonctionnement, flux, sécurité) et
[docs/integration-ia.md](../../../../../../../docs/integration-ia.md) (contrat).

## Services applicatifs

| Service | Rôle |
|---|---|
| `CopiloteConversationService` | Conversations et messages du copilote. Émet un **jeton de contexte** par message, relaie le flux SSE, journalise |
| `PlanificationVoyageService` | Collecte des dossiers et ressources libres, appel de l'agent, revalidation de chaque option par `VoyageApi.evaluerConformite` |
| `MaintenancePredictiveService` | Contexte de chaque engin (compteurs, plans et échéances, OT, sinistres, documents, voyages, carburant), appel de l'agent, enregistrement des scores |
| `AnalyseMaintenanceAutomatique` | Déclenchements automatiques (nuit, événements) : non bloquants, sans doublon |
| `ItineraireService`, `ItineraireGeometrieService` | Calcul d'itinéraire (Flask et OSRM) et tracé cartographique (OSRM) |
| `outils/*Outil` | Outils du copilote (`OutilCopilote`), découverts par `CatalogueOutilsCopilote` et filtrés par rôle |

## API REST

| Route | Rôle |
|---|---|
| `/api/v1/ia/copilote/**` | État, conversations, messages (**SSE**), avis |
| `/api/v1/ia/planification/propositions` | Propositions de voyages |
| `/api/v1/ia/maintenance/analyse` | Analyse prédictive (un engin ou la flotte) |
| `/api/v1/ia/itineraires/calcul`, `/geometrie` | Itinéraire |
| `/internal/copilote/outils/**` | **Interne** : réservée au service IA (clé de rappel + jeton de contexte) |

## Infrastructure

| Package | Rôle |
|---|---|
| `client` | Adaptateurs HTTP vers Flask (`RestClient`, clé `X-Internal-Api-Key`) et OSRM |
| `security` | `CopiloteOutilsAuthFilter`, `ContexteCopiloteStoreMemoire` |
| `config` | `CopiloteSecurityConfig`, chaîne de sécurité des routes internes |
| `planification` | `AnalyseMaintenanceNocturne` (`@Scheduled`), `ReanalyseMaintenanceListener` (événements) |
| `persistence` | `interaction_ia` (métadonnées de chaque appel) |

## Dépendances

`referential`, `order`, `dossier`, `fleet`, `driver`, `planning`, `maintenance`, `tracking`,
`carburant`, `document`, toutes via leur `api`. Aucun module ne dépend de `ai`.

## Configuration

`logiflow.ai-service.*` (connexion à Flask) et `logiflow.ai.maintenance.*` (déclenchements
automatiques). Voir [docs/agents-ia.md §11](../../../../../../../docs/agents-ia.md#11-configuration-de-référence).

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
