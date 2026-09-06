package com.logiflow.tms.driver.api;

import com.logiflow.tms.driver.api.dto.ChauffeurSummary;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Chauffeur, seul point d'entrée autorisé pour les autres modules
 * (ex. {@code planning} pour l'affectation).
 */
public interface ChauffeurApi {

  /** Consulte la vue publique d'un chauffeur, vide s'il n'existe pas. */
  Optional<ChauffeurSummary> consulter(UUID chauffeurId);

  /** Indique si le chauffeur existe et est au statut DISPONIBLE. */
  boolean estDisponible(UUID chauffeurId);

  /** Indique si le chauffeur possède une habilitation ADR de base valide à la date donnée. */
  boolean possedeHabilitationAdr(UUID chauffeurId, LocalDate date);

  /**
   * Indique si tous les documents obligatoires du chauffeur (module {@code document}) sont valides
   * à la date donnée.
   */
  boolean documentsValides(UUID chauffeurId, LocalDate date);
}
