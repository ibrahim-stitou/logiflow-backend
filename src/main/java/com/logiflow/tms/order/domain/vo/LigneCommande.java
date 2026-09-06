package com.logiflow.tms.order.domain.vo;

import java.util.Objects;
import java.util.UUID;

/** Ligne de marchandise d'une commande : marchandise du catalogue référentiel, poids et volume. */
public record LigneCommande(UUID marchandiseId, double poidsKg, double volumeM3, int nbColis) {

  public LigneCommande {
    Objects.requireNonNull(marchandiseId, "La marchandise est obligatoire");
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
}
