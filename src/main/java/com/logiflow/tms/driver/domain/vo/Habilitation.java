package com.logiflow.tms.driver.domain.vo;

import java.time.LocalDate;
import java.util.Objects;

/** Habilitation détenue par un chauffeur (permis, FIMO, ADR, visite médicale...). */
public record Habilitation(
    TypeHabilitation type, String reference, LocalDate dateObtention, LocalDate dateExpiration) {

  public Habilitation {
    Objects.requireNonNull(type, "Le type d'habilitation est obligatoire");
    Objects.requireNonNull(reference, "La référence de l'habilitation est obligatoire");
    if (reference.isBlank()) {
      throw new IllegalArgumentException("La référence de l'habilitation ne peut pas être vide");
    }
    Objects.requireNonNull(dateObtention, "La date d'obtention est obligatoire");
    Objects.requireNonNull(dateExpiration, "La date d'expiration est obligatoire");
    if (!dateExpiration.isAfter(dateObtention)) {
      throw new IllegalArgumentException(
          "La date d'expiration doit être postérieure à la date d'obtention");
    }
  }

  /**
   * Une habilitation est valide entre sa date d'obtention et sa date d'expiration (bornes
   * incluses).
   */
  public boolean estValide(LocalDate date) {
    Objects.requireNonNull(date, "La date de contrôle est obligatoire");
    return !date.isBefore(dateObtention) && !dateExpiration.isBefore(date);
  }

  public enum TypeHabilitation {
    FIMO_FCO,
    ADR_BASE,
    ADR_CITERNE,
    CARTE_CONDUCTEUR,
    VISITE_MEDICALE
  }
}
