package com.logiflow.tms.ai.domain.model;

import java.util.List;
import java.util.Objects;

/** Réponse du copilote conversationnel à une question posée en langage naturel. */
public record ReponseCopilote(String reponse, List<String> sources, Double confiance) {

  public ReponseCopilote {
    Objects.requireNonNull(reponse, "La réponse est obligatoire");
    sources = sources != null ? List.copyOf(sources) : List.of();
  }
}
