package com.logiflow.tms.carburant.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Station de distribution carburant dans le référentiel carburant. */
public final class Station {

  private final UUID id;
  private final String code;
  private String libelle;
  private String adresse;
  private boolean actif;

  private Station(UUID id, String code, String libelle, String adresse, boolean actif) {
    this.id = Objects.requireNonNull(id, "L'identifiant de la station est obligatoire");
    this.code = validerCode(code);
    this.libelle = validerLibelle(libelle);
    this.adresse = adresse;
    this.actif = actif;
  }

  public static Station creer(UUID id, String code, String libelle, String adresse) {
    return new Station(id, code, libelle, adresse, true);
  }

  public static Station reconstituer(
      UUID id, String code, String libelle, String adresse, boolean actif) {
    return new Station(id, code, libelle, adresse, actif);
  }

  public void modifier(String libelle, String adresse) {
    this.libelle = validerLibelle(libelle);
    this.adresse = adresse;
  }

  public void desactiver() {
    this.actif = false;
  }

  private static String validerCode(String code) {
    Objects.requireNonNull(code, "Le code de la station est obligatoire");
    String normalise = code.strip().toUpperCase(Locale.ROOT);
    if (normalise.isEmpty()) {
      throw new IllegalArgumentException("Le code de la station ne peut pas être vide");
    }
    return normalise;
  }

  private static String validerLibelle(String libelle) {
    Objects.requireNonNull(libelle, "Le libellé de la station est obligatoire");
    if (libelle.isBlank()) {
      throw new IllegalArgumentException("Le libellé de la station ne peut pas être vide");
    }
    return libelle.strip();
  }

  public UUID id() {
    return id;
  }

  public String code() {
    return code;
  }

  public String libelle() {
    return libelle;
  }

  public String adresse() {
    return adresse;
  }

  public boolean estActif() {
    return actif;
  }
}
