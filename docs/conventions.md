# Conventions

## Langue

- **Code** (classes, méthodes, variables, packages) : **anglais**, à l'exception assumée des
  concepts métier sans traduction naturelle et consensuelle dans l'équipe (`Voyage`, `Trajet`,
  `DossierTransport`, `OrdreTravail`, `Sinistre`, `Horaires`...), conservés en français pour rester alignés sur le
  vocabulaire métier utilisé avec les parties prenantes. Cette exception est appliquée de façon
  cohérente : un concept métier gardé en français l'est partout (classe, variable, méthode), pas
  seulement dans certains fichiers.
- **Commentaires, messages d'erreur utilisateur, documentation** : **français**.
- Les identifiants techniques purs (DTO génériques, infrastructure, tests) restent en anglais
  même quand ils manipulent des concepts métier français (ex. `SiteRepository`, pas
  `DepotSiteRepository`).

## Style de code

- Immutabilité par défaut : `record` pour les DTO, commandes et value objects ; champs `final` ;
  collections renvoyées non modifiables (`List.copyOf`, `Collectors.toUnmodifiableList()`...).
- `Optional` uniquement en **retour** de méthode, jamais en paramètre ni en champ.
- Validation systématique en entrée de méthode publique (`Objects.requireNonNull`, invariants dans
  les constructeurs compacts des records de domaine).
- `@Transactional` uniquement dans la couche `application`.
- Lombok limité à `@RequiredArgsConstructor`, `@Getter`, `@Slf4j`, `@Builder`. Interdits : `@Data`,
  `@Setter` sur les entités, `@AllArgsConstructor` sur les entités (les entités JPA écrivent leur
  constructeur complet à la main, annoté `@Builder`, plus un constructeur protégé sans argument
  pour Hibernate).
- Un contrôleur ne contient aucune logique métier : validation du DTO d'entrée, appel du service
  applicatif, mapping vers le DTO de sortie.
- Injection de dépendances exclusivement par constructeur (jamais `@Autowired` sur un champ).
- Javadoc obligatoire sur les interfaces publiques (`api/`) et les services de domaine.

## Nommage

- Packages : `com.logiflow.tms.<module>.<couche>[.<sous-package>]`, tout en minuscules.
- Tables et colonnes SQL : `snake_case` singulier. Clés primaires `id`, clés étrangères
  `<table>_id`. Contraintes nommées explicitement : `pk_`, `fk_`, `uq_`, `idx_`, `ck_`.
- DTO web : suffixe `Request` / `Response`. Commandes applicatives : suffixe `Command`.
- Entités JPA : suffixe `Entity`, jamais exposées hors de `infrastructure.persistence`.

## Commits

Convention [Conventional Commits](https://www.conventionalcommits.org/) :

```
<type>(<scope>): <description>

[corps optionnel]
```

Types utilisés : `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `ci`, `build`. Le `scope` est
généralement le nom du module (`feat(referential): ajoute la recherche paginée des sites`).

## Stratégie de branches

- `main` : toujours déployable, protégée (CI verte obligatoire avant merge).
- Une branche par fonctionnalité ou correctif : `feat/<sujet>`, `fix/<sujet>`.
- Pull request obligatoire vers `main`, avec revue d'au moins un autre membre de l'équipe.
- Pas de commit direct sur `main`.

## Definition of Done

Une fonctionnalité est terminée quand :

1. Le code respecte l'architecture hexagonale + modulaire (`HexagonalArchitectureTest`,
   `ModularityTest` verts).
2. Les règles de code transverses sont respectées (`CodingRulesTest` vert).
3. Les cas d'usage principaux et les cas d'erreur significatifs sont couverts par des tests
   (unitaires sur le domaine, intégration sur les endpoints REST concernés).
4. `./mvnw clean verify` passe sans avertissement de compilation non justifié.
5. La documentation OpenAPI reflète les nouveaux endpoints (générée automatiquement depuis les
   contrôleurs et DTO annotés).
6. Les migrations Flyway associées sont versionnées, idempotentes si possible, et rollback-aware
   (pas de suppression destructive sans confirmation explicite dans la PR).
7. La documentation est à jour :
   - le README du module concerné (`src/main/java/com/logiflow/tms/<module>/README.md`) ;
   - `docs/architecture.md` si une dépendance ou un événement entre modules change ;
   - `docs/agents-ia.md` et `docs/integration-ia.md` pour tout changement touchant l'IA ;
   - un ADR pour toute décision structurante.
8. La pull request a été revue et approuvée.
