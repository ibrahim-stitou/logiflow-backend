package com.logiflow.tms.ai.application.outils;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import java.util.List;
import java.util.Map;

/**
 * Résultat d'un outil, sérialisé tel quel vers le service IA : {@code resultats} est transmis au
 * LLM, {@code sources} devient les liens cliquables de la réponse. {@code total} peut dépasser la
 * taille de {@code resultats} (résultats tronqués).
 */
public record ResultatOutil(
    List<Map<String, Object>> resultats, long total, List<SourceCopilote> sources) {

  public ResultatOutil {
    resultats = List.copyOf(resultats);
    sources = sources != null ? List.copyOf(sources) : List.of();
  }
}
