package com.logiflow.tms.maintenance.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Prestataire externe : garage, concession, pneumaticien, expert, assureur… */
public final class Prestataire {

  /** Coordonnées et informations modifiables. */
  public record Fiche(
      String raisonSociale,
      TypePrestataire type,
      String siret,
      String contactNom,
      String telephone,
      String email,
      String adresse,
      String notes,
      boolean actif) {

    public Fiche {
      if (raisonSociale == null || raisonSociale.isBlank()) {
        throw new IllegalArgumentException("La raison sociale est obligatoire");
      }
      raisonSociale = raisonSociale.strip();
      Objects.requireNonNull(type, "Le type de prestataire est obligatoire");
      if (siret != null && !siret.isBlank() && !siret.replace(" ", "").matches("\\d{14}")) {
        throw new IllegalArgumentException("Le SIRET doit comporter 14 chiffres");
      }
    }
  }

  private final UUID id;
  private final String code;
  private Fiche fiche;

  private Prestataire(UUID id, String code, Fiche fiche) {
    this.id = Objects.requireNonNull(id, "L'identifiant du prestataire est obligatoire");
    Objects.requireNonNull(code, "Le code du prestataire est obligatoire");
    if (code.isBlank()) {
      throw new IllegalArgumentException("Le code du prestataire ne peut pas être vide");
    }
    this.code = code.strip().toUpperCase(Locale.ROOT);
    this.fiche = Objects.requireNonNull(fiche, "La fiche est obligatoire");
  }

  public static Prestataire creer(UUID id, String code, Fiche fiche) {
    return new Prestataire(id, code, fiche);
  }

  public static Prestataire reconstituer(UUID id, String code, Fiche fiche) {
    return new Prestataire(id, code, fiche);
  }

  public void modifier(Fiche nouvelle) {
    this.fiche = Objects.requireNonNull(nouvelle, "La fiche est obligatoire");
  }

  public UUID id() {
    return id;
  }

  public String code() {
    return code;
  }

  public Fiche fiche() {
    return fiche;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof Prestataire autre && id.equals(autre.id));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
