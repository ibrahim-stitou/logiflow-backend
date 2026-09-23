package com.logiflow.tms.ai.application.outils;

import java.util.HashSet;
import java.util.Set;

/**
 * Matrice des rôles pour les outils du copilote (rôles de {@code iam}, sans préfixe ROLE_). Les
 * rôles d'exploitation voient tout ; COMMERCIAL voit clients, commandes et dossiers ; ATELIER voit
 * la flotte, la maintenance et le carburant ; CHAUFFEUR n'a aucun outil.
 */
public final class RolesCopilote {

  public static final Set<String> EXPLOITATION =
      Set.of("ADMINISTRATEUR", "RESPONSABLE_EXPLOITATION", "EXPLOITANT");

  public static final Set<String> EXPLOITATION_ET_COMMERCIAL = avec("COMMERCIAL");

  public static final Set<String> EXPLOITATION_ET_ATELIER = avec("ATELIER");

  private RolesCopilote() {}

  private static Set<String> avec(String role) {
    Set<String> roles = new HashSet<>(EXPLOITATION);
    roles.add(role);
    return Set.copyOf(roles);
  }
}
