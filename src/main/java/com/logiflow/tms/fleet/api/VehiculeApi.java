package com.logiflow.tms.fleet.api;

import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import java.time.LocalDate;
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
}
