package com.logiflow.tms.fleet.domain.vo;

import java.time.LocalDate;
import java.util.Objects;

/** Document administratif attaché à un véhicule ou une remorque (carte grise, assurance...). */
public record DocumentVehicule(
    TypeDocumentVehicule type, String reference, LocalDate dateExpiration) {

  public DocumentVehicule {
    Objects.requireNonNull(type, "Le type de document est obligatoire");
    Objects.requireNonNull(reference, "La référence du document est obligatoire");
    if (reference.isBlank()) {
      throw new IllegalArgumentException("La référence du document ne peut pas être vide");
    }
    Objects.requireNonNull(dateExpiration, "La date d'expiration est obligatoire");
  }

  /** Un document est valide s'il n'est pas expiré à la date donnée (bornes incluses). */
  public boolean estValide(LocalDate date) {
    Objects.requireNonNull(date, "La date de contrôle est obligatoire");
    return !dateExpiration.isBefore(date);
  }

  public enum TypeDocumentVehicule {
    CARTE_GRISE,
    ASSURANCE,
    CONTROLE_TECHNIQUE,
    ADR
  }
}
