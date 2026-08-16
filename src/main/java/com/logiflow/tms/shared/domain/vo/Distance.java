package com.logiflow.tms.shared.domain.vo;

import java.util.Objects;

/** Distance exprimée en kilomètres, utilisée pour les trajets et étapes. */
public record Distance(double kilometres) {

  public Distance {
    if (kilometres < 0) {
      throw new IllegalArgumentException("La distance ne peut pas être négative");
    }
  }

  public static Distance zero() {
    return new Distance(0d);
  }

  public Distance plus(Distance autre) {
    Objects.requireNonNull(autre, "La distance à additionner est obligatoire");
    return new Distance(kilometres + autre.kilometres);
  }
}
