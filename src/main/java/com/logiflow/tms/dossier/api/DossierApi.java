package com.logiflow.tms.dossier.api;

import com.logiflow.tms.dossier.api.dto.DossierSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Dossier de transport, seul point d'entrée autorisé pour les autres
 * modules (ex. {@code planning} pour le groupage et l'affectation).
 */
public interface DossierApi {

  /** Consulte la vue publique d'un dossier, vide s'il n'existe pas. */
  Optional<DossierSummary> consulter(UUID dossierId);
}
