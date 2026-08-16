package com.logiflow.tms.planning.api;

import com.logiflow.tms.planning.api.dto.VoyageSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Voyage, seul point d'entrée autorisé pour les autres modules (ex.
 * {@code tracking} pour rattacher les événements d'exécution).
 */
public interface VoyageApi {

  /** Consulte la vue publique d'un voyage, vide s'il n'existe pas. */
  Optional<VoyageSummary> consulter(UUID voyageId);
}
