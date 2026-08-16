package com.logiflow.tms.fleet.api;

import com.logiflow.tms.fleet.api.dto.RemorqueSummary;
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
}
