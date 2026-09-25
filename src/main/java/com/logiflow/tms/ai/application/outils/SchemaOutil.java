package com.logiflow.tms.ai.application.outils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Petits constructeurs de JSON Schema pour décrire les paramètres des outils au LLM. */
public final class SchemaOutil {

  private SchemaOutil() {}

  /** Objet dont toutes les propriétés sont optionnelles. */
  @SafeVarargs
  public static Map<String, Object> objet(Map.Entry<String, Map<String, Object>>... proprietes) {
    Map<String, Object> props = new LinkedHashMap<>();
    for (var propriete : proprietes) {
      props.put(propriete.getKey(), propriete.getValue());
    }
    return Map.of("type", "object", "properties", props, "required", List.of());
  }

  public static Map.Entry<String, Map<String, Object>> texte(String nom, String description) {
    return Map.entry(nom, Map.of("type", "string", "description", description));
  }

  public static Map.Entry<String, Map<String, Object>> enumere(
      String nom, String description, Collection<String> valeurs) {
    return Map.entry(
        nom, Map.of("type", "string", "description", description, "enum", List.copyOf(valeurs)));
  }

  public static Map.Entry<String, Map<String, Object>> date(String nom, String description) {
    return Map.entry(
        nom,
        Map.of("type", "string", "format", "date", "description", description + " (AAAA-MM-JJ)"));
  }

  public static Map.Entry<String, Map<String, Object>> limite() {
    return Map.entry(
        "limite",
        Map.of(
            "type",
            "integer",
            "description",
            "Nombre maximal de résultats (1 à %d, défaut %d)"
                .formatted(ArgumentsOutil.LIMITE_MAX, ArgumentsOutil.LIMITE_PAR_DEFAUT)));
  }
}
