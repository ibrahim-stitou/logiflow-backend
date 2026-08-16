package com.logiflow.tms.shared.domain.vo;

import java.util.Objects;

/** Volume exprimé en mètres cubes. */
public record Volume(double m3) {

  public Volume {
    if (m3 < 0) {
      throw new IllegalArgumentException("Le volume ne peut pas être négatif");
    }
  }

  public static Volume zero() {
    return new Volume(0d);
  }

  public Volume plus(Volume autre) {
    Objects.requireNonNull(autre, "Le volume à additionner est obligatoire");
    return new Volume(m3 + autre.m3);
  }
}
