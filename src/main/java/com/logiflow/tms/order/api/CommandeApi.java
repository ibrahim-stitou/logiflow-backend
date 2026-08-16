package com.logiflow.tms.order.api;

import com.logiflow.tms.order.api.dto.CommandeSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Commande, seul point d'entrée autorisé pour les autres modules
 * (ex. {@code dossier} pour générer les dossiers de transport d'une commande confirmée).
 */
public interface CommandeApi {

  /** Consulte la vue publique d'une commande, vide si elle n'existe pas. */
  Optional<CommandeSummary> consulter(UUID commandeId);

  /** Indique si la commande existe et est au statut CONFIRMEE. */
  boolean estConfirmee(UUID commandeId);
}
