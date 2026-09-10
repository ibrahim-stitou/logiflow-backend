package com.logiflow.tms.dossier.domain.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Ligne de marchandise transportée au sein d'un dossier de transport : marchandise du catalogue
 * référentiel, poids et volume propres à cette ligne.
 *
 * <p>{@code classeAdr}, {@code numeroOnu} et {@code gerbable} sont des surcharges optionnelles :
 * {@code null} signifie qu'il faut hériter de la valeur par défaut définie sur la marchandise du
 * catalogue référentiel.
 */
public record LigneMarchandise(
    UUID marchandiseId,
    double poidsKg,
    double volumeM3,
    int nbColis,
    String classeAdr,
    String numeroOnu,
    Boolean gerbable) {

  public LigneMarchandise {
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

  /**
   * Indique si cette ligne est une matière dangereuse, en tenant compte de la surcharge locale si
   * elle est renseignée, sinon du classement par défaut de la marchandise du catalogue.
   */
  public boolean estMatiereDangereuse(boolean dangereuxParDefaut) {
    return classeAdr != null ? !classeAdr.isBlank() : dangereuxParDefaut;
  }
}
