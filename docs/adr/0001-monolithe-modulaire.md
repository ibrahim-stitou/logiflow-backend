# ADR 0001 — Monolithe modulaire (Spring Modulith) plutôt que microservices ou monolithe non structuré

- **Statut** : Acceptée
- **Date** : 2026-08-15
- **Décideurs** : Équipe LogiFlow (4 développeurs)

## Contexte

LogiFlow TMS couvre un périmètre fonctionnel large (référentiel, flotte, chauffeurs, commandes,
dossiers de transport, planning, maintenance, tracking) qui, dans une architecture microservices,
donnerait facilement 8 à 10 services distincts. L'équipe compte 4 développeurs et dispose de
8 semaines pour livrer une base de code de niveau professionnel, testée et documentée, dans un
cadre universitaire.

Trois contraintes pèsent sur la décision :

1. **Taille d'équipe et durée du projet.** Exploiter, déployer et faire évoluer plusieurs services
   indépendants (CI/CD multiplié, observabilité distribuée, gestion de la cohérence entre bases de
   données séparées) représente un coût d'infrastructure disproportionné pour 4 personnes sur
   8 semaines.
2. **Couplage métier fort entre les sous-domaines.** Le groupage de dossiers, l'affectation des
   ressources et le calcul de conformité (module `planning`) lisent en continu des données de
   `referential`, `fleet`, `driver`, `order` et `dossier`. Des appels réseau synchrones entre
   services multiplieraient la latence et la complexité de gestion des pannes partielles pour un
   gain de découplage qui n'est pas nécessaire à ce stade.
3. **Besoin de discipline architecturale malgré tout.** Un monolithe non structuré ("big ball of
   mud") est le risque symétrique : sans limites de modules imposées, un projet à 4 mains sur
   8 semaines dérive vite vers un couplage incontrôlé entre sous-domaines.

## Décision

LogiFlow TMS est construit comme un **monolithe modulaire** avec **Spring Modulith** :

- Un seul module Maven et un seul déployable (`logiflow-backend.jar` / une seule image Docker).
- Les frontières entre sous-domaines métier sont matérialisées par des packages Java
  (`com.logiflow.tms.<module>`), avec un contrat public explicite (`<module>.api`) et le reste du
  code du module inaccessible depuis l'extérieur — règle vérifiée automatiquement en CI par
  `ApplicationModules.of(...).verify()` et par des tests ArchUnit dédiés.
- Chaque module applique en interne une architecture hexagonale (`domain` / `application` /
  `infrastructure`), pour que le domaine métier reste testable indépendamment du framework et prêt
  à être extrait en service séparé si le besoin apparaît un jour.

## Conséquences

**Positives**

- Un seul pipeline de build/déploiement, une seule base de données à administrer en local et en
  CI (via Testcontainers) : coût d'infrastructure minimal, adapté à l'équipe et au calendrier.
- Les appels entre sous-domaines sont des appels de méthode Java in-process (rapides, sans
  sérialisation réseau, transactionnellement cohérents), pertinents vu le couplage métier fort
  identifié en amont.
- La discipline modulaire est vérifiée automatiquement (Spring Modulith + ArchUnit) : le risque de
  "big ball of mud" est mitigé sans payer le coût opérationnel des microservices.
- Chemin de migration préservé : parce que chaque module respecte déjà des frontières explicites
  (packages, contrat `api`, architecture hexagonale interne), un sous-domaine particulièrement
  chargé (`planning`, `tracking`) pourra être extrait en service indépendant plus tard si la charge
  ou l'équipe le justifient, sans réécriture complète.

**Négatives / compromis assumés**

- Un déploiement unique signifie qu'une mise à jour d'un module redéploie l'application entière :
  acceptable pour un projet de 8 semaines sans contrainte de disponibilité multi-équipes.
- Toutes les données vivent dans une seule instance PostgreSQL (schémas séparés par module) :
  un module ne peut pas choisir une technologie de stockage radicalement différente sans repenser
  cette décision.
- La discipline modulaire dépend de la vigilance de l'équipe et des tests automatisés ; elle n'est
  pas imposée physiquement comme le serait une séparation en services réseau.

## Alternatives écartées

- **Microservices dès le départ** : coût d'infrastructure et de coordination trop élevé pour
  4 développeurs sur 8 semaines ; le découpage en services aurait probablement suivi des
  frontières techniques plutôt que métier, faute de recul suffisant sur les besoins réels de
  `planning` et `tracking`.
- **Monolithe non structuré** ("un seul gros module Spring Boot sans limites internes") : risque
  élevé de couplage incontrôlé entre sous-domaines sur un projet à plusieurs mains ; aucune barrière
  automatisée contre les dépendances circulaires ou les fuites d'implémentation entre modules.
- **Architecture hexagonale sans découpage modulaire** (un seul `domain`/`application`/
  `infrastructure` pour toute l'application) : ne répond pas au besoin de délimiter les
  sous-domaines métier (référentiel, flotte, planning...) les uns des autres ; ne se prête pas à
  une éventuelle extraction future en services indépendants.
