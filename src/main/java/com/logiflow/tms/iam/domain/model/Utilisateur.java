package com.logiflow.tms.iam.domain.model;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Compte utilisateur applicatif. Entité racine du sous-domaine Utilisateur du module {@code iam}.
 */
public final class Utilisateur {

  private final UUID id;
  private final String login;
  private String email;
  private Set<RoleUtilisateur> roles;
  private boolean actif;

  private Utilisateur(
      UUID id, String login, String email, Set<RoleUtilisateur> roles, boolean actif) {
    this.id = Objects.requireNonNull(id, "L'identifiant de l'utilisateur est obligatoire");
    this.login = validerLogin(login);
    this.email = validerEmail(email);
    this.roles = validerRoles(roles);
    this.actif = actif;
  }

  public static Utilisateur creer(UUID id, String login, String email, Set<RoleUtilisateur> roles) {
    return new Utilisateur(id, login, email, roles, true);
  }

  public static Utilisateur reconstituer(
      UUID id, String login, String email, Set<RoleUtilisateur> roles, boolean actif) {
    return new Utilisateur(id, login, email, roles, actif);
  }

  public void changerEmail(String email) {
    this.email = validerEmail(email);
  }

  public void changerRoles(Set<RoleUtilisateur> roles) {
    this.roles = validerRoles(roles);
  }

  public void desactiver() {
    this.actif = false;
  }

  public void activer() {
    this.actif = true;
  }

  public boolean aLeRole(RoleUtilisateur role) {
    Objects.requireNonNull(role, "Le rôle est obligatoire");
    return roles.contains(role);
  }

  private static String validerLogin(String login) {
    Objects.requireNonNull(login, "Le login est obligatoire");
    String normalise = login.strip().toLowerCase(Locale.ROOT);
    if (normalise.isEmpty()) {
      throw new IllegalArgumentException("Le login ne peut pas être vide");
    }
    return normalise;
  }

  private static String validerEmail(String email) {
    Objects.requireNonNull(email, "L'email est obligatoire");
    String normalise = email.strip().toLowerCase(Locale.ROOT);
    if (!normalise.contains("@") || normalise.startsWith("@") || normalise.endsWith("@")) {
      throw new IllegalArgumentException("L'email n'est pas dans un format valide : " + email);
    }
    return normalise;
  }

  private static Set<RoleUtilisateur> validerRoles(Set<RoleUtilisateur> roles) {
    Objects.requireNonNull(roles, "Les rôles sont obligatoires");
    if (roles.isEmpty()) {
      throw new IllegalArgumentException("Un utilisateur doit avoir au moins un rôle");
    }
    return Set.copyOf(EnumSet.copyOf(roles));
  }

  public UUID id() {
    return id;
  }

  public String login() {
    return login;
  }

  public String email() {
    return email;
  }

  public Set<RoleUtilisateur> roles() {
    return roles;
  }

  public boolean estActif() {
    return actif;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Utilisateur utilisateur)) {
      return false;
    }
    return id.equals(utilisateur.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
