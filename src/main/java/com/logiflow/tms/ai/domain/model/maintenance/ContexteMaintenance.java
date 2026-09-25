package com.logiflow.tms.ai.domain.model.maintenance;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Contexte transmis à l'agent de maintenance prédictive : état de chaque véhicule, plans
 * d'entretien, historique des ordres de travail, documents, activité des 90 derniers jours et
 * voyages planifiés.
 */
public record ContexteMaintenance(
    Instant dateReference, int horizonJours, List<Vehicule> vehicules, String correlationId) {

  /** Copie portant l'identifiant de corrélation (posé par l'adaptateur). */
  public ContexteMaintenance avecCorrelationId(String identifiant) {
    return new ContexteMaintenance(dateReference, horizonJours, vehicules, identifiant);
  }

  public record Plan(
      String id,
      String libelle,
      Integer periodiciteKm,
      Integer periodiciteMois,
      int seuilAlerteKm,
      int dureeEstimeeMin) {}

  public record Ordre(String type, String statut, Instant datePlanifiee) {}

  public record Document(String type, LocalDate dateExpiration) {}

  public record VoyagePlanifie(
      String reference, Instant depart, Instant arrivee, double distanceKm) {}

  public record Vehicule(
      String id,
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
      List<VoyagePlanifie> voyagesPlanifies) {}
}
