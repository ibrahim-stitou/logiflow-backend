package com.logiflow.tms.ai.domain.model.copilote;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Message d'une conversation du copilote. {@code role} : {@code user} ou {@code assistant} ; {@code
 * statut} : {@code complet}, {@code interrompu}, {@code erreur} ou {@code en_cours}.
 */
public record MessageCopilote(
    UUID id,
    String role,
    String contenu,
    String statut,
    List<SourceCopilote> sources,
    Instant creeLe) {

  public MessageCopilote {
    sources = sources != null ? List.copyOf(sources) : List.of();
  }
}
