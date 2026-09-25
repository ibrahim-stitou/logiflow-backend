package com.logiflow.tms.maintenance.api;

import com.logiflow.tms.maintenance.api.dto.OrdreTravailSummary;
import com.logiflow.tms.maintenance.api.dto.PlanEntretienSummary;
import com.logiflow.tms.maintenance.api.dto.ScoreSanteSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du module {@code maintenance}, seul point d'entrée autorisé pour les autres
 * modules.
 */
public interface MaintenanceApi {

  /** Consulte le dernier score de santé calculé pour un véhicule, vide si aucun n'existe encore. */
  Optional<ScoreSanteSummary> dernierScoreSante(UUID vehiculeId);

  /** Ordres de travail, d'un véhicule ou de toute la flotte ({@code vehiculeId} null). */
  Page<OrdreTravailSummary> ordresTravail(UUID vehiculeId, PageRequest pageRequest);

  /** Plans d'entretien, d'un véhicule ou de toute la flotte ({@code vehiculeId} null). */
  Page<PlanEntretienSummary> plansEntretien(UUID vehiculeId, PageRequest pageRequest);

  /**
   * Enregistre un score de santé calculé (agent de maintenance prédictive) ; le statut est dérivé
   * du score et du kilométrage restant avant échéance.
   */
  ScoreSanteSummary enregistrerScoreSante(
      UUID vehiculeId,
      double score,
      int kmAvantEcheance,
      LocalDate dateEcheanceProjetee,
      String recommandation);
}
