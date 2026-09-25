# Module `shared` — socle technique

Module **ouvert** (`@ApplicationModule(type = OPEN)`) : utilisable depuis n'importe quelle couche
de n'importe quel module. Il ne contient **aucune règle métier**.

## Contenu

| Package | Rôle |
|---|---|
| `domain.vo` | Value objects communs : `Money` (montant + devise), `GeoPoint` (WGS84), `Reference` (`PREFIXE-AAAA-nnnnnn`), `Immatriculation`, `Poids`, `Volume`, `Capacite`, `Distance`, `TimeWindow` |
| `domain.exception` | `NotFoundException` (404), `ValidationException` (400), `ConflictException` (409), `BusinessException` (422), `ServiceIndisponibleException` (503), exceptions métier spécifiques (capacité de remorque, détour d'itinéraire) |
| `application` | `Page`, `PageRequest` (pagination indépendante de Spring Data) |
| `infrastructure.web` | `GlobalExceptionHandler` (RFC 7807), `CorrelationIdFilter` (`X-Correlation-Id`), `PageResponse` |
| `infrastructure.security` | `SecurityContextService` : utilisateur courant (sub, nom, rôles) depuis le JWT |
| `infrastructure.persistence` | Audit JPA, `JpaReferenceSequenceStore` (numérotation des références métier) |

## Règles

- `application` et `domain` des autres modules ne doivent pas dépendre de
  `shared.infrastructure` (vérifié par ArchUnit).
- Tout ajout ici doit être réellement transverse ; sinon, il appartient à un module métier.

---
Vue d'ensemble des modules : [docs/architecture.md](../../../../../../../docs/architecture.md).
