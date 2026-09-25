package com.logiflow.tms.driver.domain.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Catégories du permis de conduire utiles au transport de marchandises : C1/C pour les porteurs,
 * C1E/CE pour les ensembles articulés (tracteur + semi-remorque).
 */
public enum CategoriePermis {
  B,
  C1,
  C,
  C1E,
  CE;

  /** Permis requis pour conduire un tracteur attelé à une semi-remorque. */
  public static final CategoriePermis ENSEMBLE_ARTICULE = CE;

  /**
   * Lit une liste saisie librement (« C, CE », « c;ce »…). Les valeurs inconnues sont ignorées pour
   * rester tolérant aux données historiques en texte libre.
   */
  public static Set<CategoriePermis> depuisTexte(String texte) {
    if (texte == null || texte.isBlank()) {
      return EnumSet.noneOf(CategoriePermis.class);
    }
    return Arrays.stream(texte.toUpperCase(Locale.ROOT).split("[^A-Z0-9]+"))
        .filter(valeur -> !valeur.isBlank())
        .flatMap(
            valeur -> Arrays.stream(values()).filter(categorie -> categorie.name().equals(valeur)))
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(CategoriePermis.class)));
  }

  /** Format de stockage : noms séparés par des virgules, dans l'ordre de l'énumération. */
  public static String versTexte(Set<CategoriePermis> categories) {
    return categories.stream().sorted().map(Enum::name).collect(Collectors.joining(","));
  }

  /** Un permis CE couvre C, un permis C couvre C1, etc. (équivalences réglementaires usuelles). */
  public boolean estCouvertePar(Set<CategoriePermis> detenues) {
    return switch (this) {
      case B -> !detenues.isEmpty();
      case C1 -> detenues.stream().anyMatch(c -> c != B);
      case C -> detenues.contains(C) || detenues.contains(CE);
      case C1E -> detenues.contains(C1E) || detenues.contains(CE);
      case CE -> detenues.contains(CE);
    };
  }
}
