package com.logiflow.tms.fleet.api;

import com.logiflow.tms.fleet.api.dto.RemorqueEtatSummary;
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

  /** État (compteurs, âge) des remorques en service, pour la maintenance. */
  List<RemorqueEtatSummary> listerPourMaintenance();

  /**
   * Immobilise l'engin pour la maintenance ({@code sinistre} = false → EN_MAINTENANCE) ou suite à
   * un sinistre (true → IMMOBILISE). Sans effet sur un engin hors service.
   */
  void signalerImmobilisation(UUID id, boolean sinistre);

  /** Remet l'engin DISPONIBLE s'il était EN_MAINTENANCE ou IMMOBILISE. */
  void signalerRemiseEnService(UUID id);

  /** Relève les compteurs ; une valeur inférieure à la valeur actuelle est ignorée. */
  void releverCompteurs(UUID id, Integer kilometrage, Integer heures);
}
