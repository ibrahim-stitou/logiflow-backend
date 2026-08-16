package com.logiflow.tms.iam.api;

import com.logiflow.tms.iam.api.dto.UtilisateurSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Utilisateur, seul point d'entrée autorisé pour les autres modules.
 */
public interface UtilisateurApi {

  /** Consulte la vue publique d'un utilisateur, vide s'il n'existe pas. */
  Optional<UtilisateurSummary> consulter(UUID utilisateurId);

  /** Consulte la vue publique d'un utilisateur par son login, vide s'il n'existe pas. */
  Optional<UtilisateurSummary> consulterParLogin(String login);
}
