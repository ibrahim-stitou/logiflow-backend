package com.logiflow.tms.ai.domain.model.maintenance;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Réponse de l'agent de maintenance prédictive : analyse par véhicule (du plus à risque au moins à
 * risque) et synthèse de flotte. {@code sourceRedaction} vaut LLM ou GABARIT.
 */
public record ResultatMaintenance(
    List<AnalyseVehicule> vehicules, String synthese, String sourceRedaction) {

  public ResultatMaintenance {
    vehicules = vehicules == null ? List.of() : List.copyOf(vehicules);
  }

  public record Echeance(
      String libelle, Integer kmRestant, LocalDate dateEcheance, boolean enAlerte) {}

  public record Recommandation(
      String type,
      String libelle,
      String priorite,
      LocalDate avantLe,
      Instant creneauDebut,
      Instant creneauFin,
      int dureeMin,
      String justification,
      boolean dejaPlanifie) {}

  public record AnalyseVehicule(
      String vehiculeId,
      String immatriculation,
      double score,
      String statut,
      double kmParJour,
      Double consommationL100,
      Integer kmAvantEcheance,
      LocalDate dateEcheance,
      List<Echeance> echeances,
      List<String> anomalies,
      List<Recommandation> recommandations,
      String explication) {}
}
