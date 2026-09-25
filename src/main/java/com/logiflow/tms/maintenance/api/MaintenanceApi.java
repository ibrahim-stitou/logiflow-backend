package com.logiflow.tms.maintenance.api;

import com.logiflow.tms.maintenance.api.dto.CoutsMaintenanceSummary;
import com.logiflow.tms.maintenance.api.dto.IndisponibiliteSummary;
import com.logiflow.tms.maintenance.api.dto.OrdreTravailSummary;
import com.logiflow.tms.maintenance.api.dto.PlanEntretienSummary;
import com.logiflow.tms.maintenance.api.dto.ScoreSanteSummary;
import com.logiflow.tms.maintenance.api.dto.SinistreSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du module {@code maintenance}, seul point d'entrée autorisé pour les autres
 * modules. Les engins sont désignés par leur identifiant (véhicule ou remorque).
 */
public interface MaintenanceApi {

  /** Dernier score de santé calculé pour un engin, vide si aucun n'existe encore. */
  Optional<ScoreSanteSummary> dernierScoreSante(UUID enginId);

  /** Ordres de travail d'un engin ou de toute la flotte ({@code enginId} null), récents d'abord. */
  Page<OrdreTravailSummary> ordresTravail(UUID enginId, PageRequest pageRequest);

  /** Plans d'entretien et prochaine échéance, d'un engin ou de toute la flotte. */
  Page<PlanEntretienSummary> plansEntretien(UUID enginId, PageRequest pageRequest);

  /**
   * Échéances des plans actifs de la flotte : échues, en alerte, ou tombant dans l'horizon (null =
   * tous les plans), des plus urgentes aux plus lointaines.
   */
  List<PlanEntretienSummary> echeances(Integer horizonJours);

  /**
   * Coûts de maintenance et sinistralité entre deux dates incluses ; {@code typeEngin} (VEHICULE ou
   * REMORQUE) facultatif.
   */
  CoutsMaintenanceSummary couts(LocalDate debut, LocalDate fin, String typeEngin);

  /** Sinistres survenus entre deux dates incluses, d'un engin ou de toute la flotte. */
  List<SinistreSummary> sinistres(UUID enginId, LocalDate debut, LocalDate fin);

  /** Engins retenus à l'atelier pendant [debut, fin] (planification des voyages). */
  List<IndisponibiliteSummary> indisponibilites(Instant debut, Instant fin);

  /**
   * Enregistre un score de santé calculé (agent de maintenance prédictive) ; le statut est dérivé
   * du score et du kilométrage restant avant échéance.
   */
  ScoreSanteSummary enregistrerScoreSante(
      UUID enginId,
      double score,
      int kmAvantEcheance,
      LocalDate dateEcheanceProjetee,
      String recommandation);
}
