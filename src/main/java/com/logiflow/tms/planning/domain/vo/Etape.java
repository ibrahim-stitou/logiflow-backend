package com.logiflow.tms.planning.domain.vo;

import com.logiflow.tms.planning.domain.model.TypeEtape;
import java.time.Instant;
import java.util.Objects;

/** Étape ordonnée d'un trajet, avec horaires estimés et distance depuis l'étape précédente. */
public record Etape(
    int ordre,
    TypeEtape type,
    Instant eta,
    Instant etd,
    double distanceDepuisPrecedenteKm,
    double chargeApresKg) {

  public Etape {
    if (ordre < 0) {
      throw new IllegalArgumentException("L'ordre de l'étape ne peut pas être négatif");
    }
    Objects.requireNonNull(type, "Le type de l'étape est obligatoire");
    Objects.requireNonNull(eta, "L'heure d'arrivée estimée est obligatoire");
    if (distanceDepuisPrecedenteKm < 0) {
      throw new IllegalArgumentException(
          "La distance depuis l'étape précédente ne peut pas être négative");
    }
    if (chargeApresKg < 0) {
      throw new IllegalArgumentException("La charge après étape ne peut pas être négative");
    }
  }
}
