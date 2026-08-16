package com.logiflow.tms.shared.domain.vo;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Référence métier lisible, format {@code <PREFIXE>-<ANNEE>-<SEQUENCE 6 chiffres>}. */
public record Reference(String valeur) {

  private static final Pattern FORMAT = Pattern.compile("^[A-Z]{2,5}-\\d{4}-\\d{6}$");

  public Reference {
    Objects.requireNonNull(valeur, "La référence est obligatoire");
    valeur = valeur.strip().toUpperCase(Locale.ROOT);
    if (!FORMAT.matcher(valeur).matches()) {
      throw new IllegalArgumentException(
          "Le format de référence est invalide, attendu PREFIXE-AAAA-999999 : " + valeur);
    }
  }

  public static Reference generer(String prefixe, int annee, long sequence) {
    Objects.requireNonNull(prefixe, "Le préfixe est obligatoire");
    if (annee < 1000 || annee > 9999) {
      throw new IllegalArgumentException("L'année doit être exprimée sur 4 chiffres");
    }
    if (sequence < 0 || sequence > 999_999) {
      throw new IllegalArgumentException("La séquence doit être comprise entre 0 et 999999");
    }
    return new Reference(
        "%s-%04d-%06d".formatted(prefixe.toUpperCase(Locale.ROOT), annee, sequence));
  }
}
