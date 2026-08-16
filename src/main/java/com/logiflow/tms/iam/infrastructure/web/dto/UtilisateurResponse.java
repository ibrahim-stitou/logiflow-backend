package com.logiflow.tms.iam.infrastructure.web.dto;

import com.logiflow.tms.iam.domain.model.RoleUtilisateur;
import com.logiflow.tms.iam.domain.model.Utilisateur;
import java.util.Set;
import java.util.UUID;

public record UtilisateurResponse(
    UUID id, String login, String email, Set<RoleUtilisateur> roles, boolean actif) {

  public static UtilisateurResponse depuis(Utilisateur utilisateur) {
    return new UtilisateurResponse(
        utilisateur.id(),
        utilisateur.login(),
        utilisateur.email(),
        utilisateur.roles(),
        utilisateur.estActif());
  }
}
