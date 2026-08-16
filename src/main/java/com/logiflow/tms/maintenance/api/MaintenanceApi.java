package com.logiflow.tms.maintenance.api;

import com.logiflow.tms.maintenance.api.dto.ScoreSanteSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du module {@code maintenance}, seul point d'entrée autorisé pour les autres
 * modules.
 */
public interface MaintenanceApi {

  /** Consulte le dernier score de santé calculé pour un véhicule, vide si aucun n'existe encore. */
  Optional<ScoreSanteSummary> dernierScoreSante(UUID vehiculeId);
}
