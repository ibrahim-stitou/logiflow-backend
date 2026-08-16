package com.logiflow.tms.shared.domain.vo;

import java.util.Objects;

/** Poids exprimé en kilogrammes. */
public record Poids(double kg) {

  public Poids {
    if (kg < 0) {
      throw new IllegalArgumentException("Le poids ne peut pas être négatif");
    }
  }

  public static Poids zero() {
    return new Poids(0d);
  }

  public Poids plus(Poids autre) {
    Objects.requireNonNull(autre, "Le poids à additionner est obligatoire");
    return new Poids(kg + autre.kg);
  }
}
