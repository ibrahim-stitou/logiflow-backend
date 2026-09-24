package com.logiflow.tms.fleet.api;

import com.logiflow.tms.fleet.api.dto.VehiculePlanificationSummary;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Véhicule, seul point d'entrée autorisé pour les autres modules
 * (ex. {@code planning} pour l'affectation, {@code maintenance} pour les compteurs).
 */
public interface VehiculeApi {

  /** Consulte la vue publique d'un véhicule, vide s'il n'existe pas. */
  Optional<VehiculeSummary> consulter(UUID vehiculeId);

  /** Indique si le véhicule existe et est au statut DISPONIBLE. */
  boolean estDisponible(UUID vehiculeId);

  /** Indique si tous les documents obligatoires du véhicule sont valides à la date donnée. */
  boolean documentsValides(UUID vehiculeId, LocalDate date);

  /**
   * Recherche paginée pour la consultation transverse (copilote IA). {@code statut} optionnel
   * ({@code null} = tous), doit appartenir à {@link #statutsConnus()} ; {@code texte} optionnel.
   */
  Page<VehiculeSummary> rechercher(String texte, String statut, PageRequest pageRequest);

  /** Valeurs possibles du filtre de {@link #rechercher}. */
  List<String> statutsConnus();

  /** Vue de planification d'un véhicule, documents évalués à la date donnée. */
  Optional<VehiculePlanificationSummary> consulterPourPlanification(
      UUID vehiculeId, LocalDate date);

  /** Véhicules en service (hors HORS_SERVICE), documents évalués à la date donnée. */
  List<VehiculePlanificationSummary> listerPourPlanification(LocalDate date);
}
