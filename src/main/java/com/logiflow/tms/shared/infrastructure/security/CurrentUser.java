package com.logiflow.tms.shared.infrastructure.security;

import java.util.Set;

/** Représentation technique de l'utilisateur authentifié courant, extraite du JWT. */
public record CurrentUser(String sujet, String nomAffichage, Set<String> roles) {

  public static final String SYSTEME = "system";

  public boolean aLeRole(String role) {
    return roles.contains(role);
  }
}
