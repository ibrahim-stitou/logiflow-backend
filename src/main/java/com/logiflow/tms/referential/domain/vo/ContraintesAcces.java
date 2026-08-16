package com.logiflow.tms.referential.domain.vo;

/** Contraintes physiques et réglementaires d'accès à un site (gabarit, restrictions). */
public record ContraintesAcces(
    Double hauteurMaxMetres,
    Double poidsMaxTonnes,
    boolean interditPoidsLourd,
    String instructions) {

  public ContraintesAcces {
    if (hauteurMaxMetres != null && hauteurMaxMetres <= 0) {
      throw new IllegalArgumentException("La hauteur maximale doit être strictement positive");
    }
    if (poidsMaxTonnes != null && poidsMaxTonnes <= 0) {
      throw new IllegalArgumentException("Le poids maximal doit être strictement positif");
    }
  }

  public static ContraintesAcces aucune() {
    return new ContraintesAcces(null, null, false, null);
  }
}
