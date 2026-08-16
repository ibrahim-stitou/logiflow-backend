package com.logiflow.tms.referential.api;

import com.logiflow.tms.referential.api.dto.ClientSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Client, seul point d'entrée autorisé pour les autres modules (ex.
 * {@code order} pour valider l'existence d'un client lors de la création d'une commande).
 */
public interface ClientApi {

  /** Consulte la vue publique d'un client, vide s'il n'existe pas. */
  Optional<ClientSummary> consulter(UUID clientId);

  /** Indique si le client existe et est actif. */
  boolean estActif(UUID clientId);
}
