package com.logiflow.tms.referential.api;

import com.logiflow.tms.referential.api.dto.SiteSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Site, seul point d'entrée autorisé pour les autres modules (ex.
 * {@code planning} pour valider l'accessibilité d'un site lors d'une affectation).
 */
public interface SiteApi {

  /** Consulte la vue publique d'un site, vide s'il n'existe pas. */
  Optional<SiteSummary> consulter(UUID siteId);

  /** Indique si le site existe et est actif. */
  boolean estActif(UUID siteId);
}
