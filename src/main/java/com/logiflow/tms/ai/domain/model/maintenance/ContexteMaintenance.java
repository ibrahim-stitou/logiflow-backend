package com.logiflow.tms.ai.domain.model.maintenance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Contexte transmis à l'agent de maintenance prédictive : état de chaque engin (véhicule ou
 * remorque), plans d'entretien avec leur échéance calculée par le module maintenance, historique
 * des ordres de travail, documents, sinistres des 12 derniers mois, activité des 90 derniers jours
 * et voyages planifiés.
 */
public record ContexteMaintenance(
    Instant dateReference, int horizonJours, List<Vehicule> vehicules, String correlationId) {

  /** Copie portant l'identifiant de corrélation (posé par l'adaptateur). */
  public ContexteMaintenance avecCorrelationId(String identifiant) {
    return new ContexteMaintenance(dateReference, horizonJours, vehicules, identifiant);
  }

  /**
   * Plan d'entretien : la dernière réalisation et l'échéance ({@code etat} OK, ALERTE ou ECHU) sont
   * calculées par le module maintenance, l'agent n'a plus à les estimer.
   */
  public record Plan(
      String id,
      String libelle,
      String type,
      Integer periodiciteKm,
      Integer periodiciteMois,
      Integer periodiciteHeures,
      int seuilAlerteKm,
      int dureeEstimeeMin,
      LocalDate derniereDate,
      Integer derniereKm,
      Integer kmRestant,
      LocalDate dateEcheance,
      String etat) {}

  /** Ordre de travail ; {@code datePlanifiee} vaut la fin réelle s'il est terminé. */
  public record Ordre(
      String reference,
      String type,
      String nature,
      String statut,
      String origine,
      String planId,
      Instant datePlanifiee,
      boolean immobilisation,
      BigDecimal coutTtc) {}

  public record Document(String type, LocalDate dateExpiration) {}

  public record Sinistre(
      String reference,
      LocalDate dateSurvenance,
      String type,
      String gravite,
      String responsabilite,
      String statut,
      boolean enginImmobilise,
      BigDecimal coutNet) {}

  public record VoyagePlanifie(
      String reference, Instant depart, Instant arrivee, double distanceKm) {}

  /**
   * Engin à analyser ({@code typeEngin} VEHICULE ou REMORQUE) ; pour une remorque, {@code type} est
   * la carrosserie et {@code heuresMoteur} les heures du groupe froid.
   */
  public record Vehicule(
      String id,
      String typeEngin,
      String immatriculation,
      String type,
      String statut,
      int kilometrage,
      int heuresMoteur,
      Integer anneeMiseEnCirculation,
      double kmRealises,
      double litresConsommes,
      List<Plan> plans,
      List<Ordre> ordres,
      List<Document> documents,
      List<Sinistre> sinistres,
      List<VoyagePlanifie> voyagesPlanifies) {}
}
