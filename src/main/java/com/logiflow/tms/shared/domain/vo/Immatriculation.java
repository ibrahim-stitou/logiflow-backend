package com.logiflow.tms.shared.domain.vo;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Plaque d'immatriculation au format français (SIV) : AA-123-AA. */
public record Immatriculation(String valeur) {

  private static final Pattern FORMAT_SIV = Pattern.compile("^[A-Z]{2}-\\d{3}-[A-Z]{2}$");

  public Immatriculation {
    Objects.requireNonNull(valeur, "L'immatriculation est obligatoire");
    valeur = valeur.strip().toUpperCase(Locale.ROOT);
    if (!FORMAT_SIV.matcher(valeur).matches()) {
      throw new IllegalArgumentException(
          "Le format d'immatriculation est invalide, attendu AA-123-AA : " + valeur);
    }
  }
}
