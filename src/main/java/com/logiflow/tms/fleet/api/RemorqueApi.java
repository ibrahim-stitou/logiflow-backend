package com.logiflow.tms.fleet.api;

import com.logiflow.tms.fleet.api.dto.RemorquePlanificationSummary;
import com.logiflow.tms.fleet.api.dto.RemorqueSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Remorque, seul point d'entrée autorisé pour les autres modules.
 */
public interface RemorqueApi {

  /** Consulte la vue publique d'une remorque, vide si elle n'existe pas. */
  Optional<RemorqueSummary> consulter(UUID remorqueId);

  /** Indique si la remorque existe et est au statut DISPONIBLE. */
  boolean estDisponible(UUID remorqueId);

  /**
   * Recherche paginée pour la consultation transverse (copilote IA). {@code statut} optionnel
   * ({@code null} = tous), doit appartenir à {@link #statutsConnus()} ; {@code texte} optionnel.
   */
  Page<RemorqueSummary> rechercher(String texte, String statut, PageRequest pageRequest);

  /** Valeurs possibles du filtre de {@link #rechercher}. */
  List<String> statutsConnus();

  /** Indique si tous les documents de la remorque sont valides à la date donnée. */
  boolean documentsValides(UUID remorqueId, LocalDate date);

  /** Vue de planification d'une remorque, documents évalués à la date donnée. */
  Optional<RemorquePlanificationSummary> consulterPourPlanification(
      UUID remorqueId, LocalDate date);

  /** Remorques en service (hors HORS_SERVICE), documents évalués à la date donnée. */
  List<RemorquePlanificationSummary> listerPourPlanification(LocalDate date);
}
