package com.logiflow.tms.shared.domain.vo;

import java.util.Objects;

/** Capacité de chargement d'une ressource de transport (remorque, voyage). */
public record Capacite(int poidsKg, double volumeM3, int positionsPalettes) {

  public Capacite {
    if (poidsKg < 0) {
      throw new IllegalArgumentException("Le poids ne peut pas être négatif");
    }
    if (volumeM3 < 0) {
      throw new IllegalArgumentException("Le volume ne peut pas être négatif");
    }
    if (positionsPalettes < 0) {
      throw new IllegalArgumentException(
          "Le nombre de positions palettes ne peut pas être négatif");
    }
  }

  public static Capacite zero() {
    return new Capacite(0, 0.0, 0);
  }

  public Capacite plus(Capacite autre) {
    Objects.requireNonNull(autre, "La capacité à additionner est obligatoire");
    return new Capacite(
        poidsKg + autre.poidsKg,
        volumeM3 + autre.volumeM3,
        positionsPalettes + autre.positionsPalettes);
  }

  public Capacite moins(Capacite autre) {
    Objects.requireNonNull(autre, "La capacité à soustraire est obligatoire");
    return new Capacite(
        poidsKg - autre.poidsKg,
        volumeM3 - autre.volumeM3,
        positionsPalettes - autre.positionsPalettes);
  }

  /** Indique si cette capacité tient dans la capacité disponible fournie. */
  public boolean tientDans(Capacite disponible) {
    Objects.requireNonNull(disponible, "La capacité disponible est obligatoire");
    return poidsKg <= disponible.poidsKg
        && volumeM3 <= disponible.volumeM3
        && positionsPalettes <= disponible.positionsPalettes;
  }

  /** Taux de remplissage par rapport à une capacité maximale, contrainte la plus limitante. */
  public double tauxRemplissage(Capacite maximum) {
    Objects.requireNonNull(maximum, "La capacité maximale est obligatoire");
    double tauxPoids = maximum.poidsKg == 0 ? 0d : (double) poidsKg / maximum.poidsKg;
    double tauxVolume = maximum.volumeM3 == 0 ? 0d : volumeM3 / maximum.volumeM3;
    return Math.max(tauxPoids, tauxVolume);
  }
}
