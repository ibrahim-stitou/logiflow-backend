package com.logiflow.tms.ai.application.outils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Construction de lignes de résultat ordonnées ; les valeurs nulles sont omises (moins de bruit).
 */
final class Lignes {

  private Lignes() {}

  static Map<String, Object> ligne(Object... clesValeurs) {
    if (clesValeurs.length % 2 != 0) {
      throw new IllegalArgumentException("Nombre pair d'arguments attendu (clé, valeur)");
    }
    Map<String, Object> ligne = new LinkedHashMap<>();
    for (int i = 0; i < clesValeurs.length; i += 2) {
      if (clesValeurs[i + 1] != null) {
        ligne.put((String) clesValeurs[i], clesValeurs[i + 1]);
      }
    }
    return ligne;
  }
}
