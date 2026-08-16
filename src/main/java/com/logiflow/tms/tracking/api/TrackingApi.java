package com.logiflow.tms.tracking.api;

import com.logiflow.tms.tracking.api.dto.EvenementVoyageSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du module {@code tracking}, seul point d'entrée autorisé pour les autres modules.
 */
public interface TrackingApi {

  /** Dernier événement connu pour un voyage, vide si aucun n'a encore été déclaré. */
  Optional<EvenementVoyageSummary> dernierEvenement(UUID voyageId);
}
