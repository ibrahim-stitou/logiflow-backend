# ADR 0005 — Agent de planification de voyage (remplace l'agent de groupage)

- **Statut** : accepté
- **Date** : 2026-09-24

## Contexte

L'agent de groupage ne proposait que des paires de dossiers compatibles (poids, volume, ADR),
sans géographie, sans dates ni ressources : il ne répondait pas au besoin réel de l'exploitant,
qui est de **construire un voyage**. Par ailleurs, la création de voyage avait des lacunes
bloquantes pour toute planification sérieuse :

- aucun arrêt n'était persisté (la vue capacité et l'ajout de dossier échouaient sur les voyages
  créés depuis l'interface) ;
- la disponibilité des ressources se jugeait sur le statut du jour, sans contrôle de
  chevauchement entre voyages ;
- plusieurs règles n'étaient pas vérifiées (remorque, carrosserie/température, tracteur sans
  remorque, documents à la date de départ, un seul chauffeur possible).

## Décision

1. **Fondations dans `planning`.** Les arrêts sont construits et persistés à la création (depuis
   les sites des dossiers, ou dans l'ordre imposé par la requête) ; la disponibilité se juge sur
   la **période** du voyage ; toutes les règles sont regroupées dans `ConformiteVoyageService`,
   exposé à blanc (`POST /voyages/conformite`) et via `VoyageApi.evaluerConformite`.
2. **Un agent de planification remplace le groupage.** Pour une période et un type de voyage, il
   propose N voyages complets et comparés (dossiers, arrêts avec heures, tracteur + remorque,
   chauffeurs, indicateurs). Le groupage devient une étape interne de cet agent.
3. **Solveur déterministe + LLM rédacteur.** Le calcul (matrice OSRM `/table`, groupes par
   insertion gloutonne, ordre des arrêts avec précédence, horaires avec pauses réglementaires,
   choix des ressources, sélection d'options par objectif) est déterministe et testable. Le LLM
   ne rédige que les justifications et la comparaison, avec un gabarit de repli : un LLM ne
   décide ni d'une capacité ni d'une conformité.
4. **Spring revalide chaque proposition** avec les mêmes règles que la création. Le contexte
   envoyé à l'agent est déjà filtré (ressources libres et exploitables), et l'agent n'a aucun
   accès au backend ni à la base TMS.
5. **L'humain décide.** Choisir une proposition pré-remplit le formulaire de création : rien n'est
   créé sans relecture. La planification manuelle reste disponible, avec les ressources libres
   sur la période et le contrôle de conformité en direct.

## Conséquences

- La création de voyage est plus stricte (chevauchements, carrosserie, tracteur sans remorque…) ;
  les fenêtres horaires restent des avertissements pour ne pas bloquer les cas limites.
- Les tests d'intégration créent désormais de vrais sites pour les segments des dossiers.
- Sans service IA, la planification assistée répond 503 et l'écran renvoie vers le mode manuel ;
  sans OSRM, les distances sont estimées (Haversine × 1,3 à 70 km/h) et signalées comme telles.
- Hors périmètre : repos journaliers des voyages de plusieurs jours (seulement signalés),
  optimisation globale multi-voyages, maintenance prédictive.
