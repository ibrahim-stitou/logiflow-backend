package com.logiflow.tms.referential.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Client donneur d'ordre du transport. Entité racine du sous-domaine Client. */
public final class Client {

  private final UUID id;
  private final String code;
  private String raisonSociale;
  private boolean actif;

  private Client(UUID id, String code, String raisonSociale, boolean actif) {
    this.id = Objects.requireNonNull(id, "L'identifiant du client est obligatoire");
    this.code = validerCode(code);
    this.raisonSociale = validerRaisonSociale(raisonSociale);
    this.actif = actif;
  }

  public static Client creer(UUID id, String code, String raisonSociale) {
    return new Client(id, code, raisonSociale, true);
  }

  public static Client reconstituer(UUID id, String code, String raisonSociale, boolean actif) {
    return new Client(id, code, raisonSociale, actif);
  }

  public void renommer(String raisonSociale) {
    this.raisonSociale = validerRaisonSociale(raisonSociale);
  }

  public void desactiver() {
    this.actif = false;
  }

  public void activer() {
    this.actif = true;
  }

  private static String validerCode(String code) {
    Objects.requireNonNull(code, "Le code du client est obligatoire");
    String normalise = code.strip().toUpperCase(Locale.ROOT);
    if (normalise.isEmpty()) {
      throw new IllegalArgumentException("Le code du client ne peut pas être vide");
    }
    return normalise;
  }

  private static String validerRaisonSociale(String raisonSociale) {
    Objects.requireNonNull(raisonSociale, "La raison sociale est obligatoire");
    if (raisonSociale.isBlank()) {
      throw new IllegalArgumentException("La raison sociale ne peut pas être vide");
    }
    return raisonSociale.strip();
  }

  public UUID id() {
    return id;
  }

  public String code() {
    return code;
  }

  public String raisonSociale() {
    return raisonSociale;
  }

  public boolean estActif() {
    return actif;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Client client)) {
      return false;
    }
    return id.equals(client.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
