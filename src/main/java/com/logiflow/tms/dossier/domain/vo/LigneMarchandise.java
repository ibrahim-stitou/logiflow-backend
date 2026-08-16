package com.logiflow.tms.dossier.domain.vo;

import java.util.Objects;

/** Ligne de marchandise transportée au sein d'un dossier de transport. */
public record LigneMarchandise(
    String designation,
    double poidsKg,
    double volumeM3,
    int nbColis,
    String classeAdr,
    String numeroOnu,
    boolean gerbable) {

  public LigneMarchandise {
    Objects.requireNonNull(designation, "La désignation de la marchandise est obligatoire");
    if (designation.isBlank()) {
      throw new IllegalArgumentException("La désignation de la marchandise ne peut pas être vide");
    }
    if (poidsKg < 0) {
      throw new IllegalArgumentException("Le poids ne peut pas être négatif");
    }
    if (volumeM3 < 0) {
      throw new IllegalArgumentException("Le volume ne peut pas être négatif");
    }
    if (nbColis < 0) {
      throw new IllegalArgumentException("Le nombre de colis ne peut pas être négatif");
    }
  }

  public boolean estMatiereDangereuse() {
    return classeAdr != null && !classeAdr.isBlank();
  }
}
