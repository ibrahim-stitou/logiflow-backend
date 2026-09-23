package com.logiflow.tms.ai.domain.model.copilote;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Conversation du copilote. Persistée par le service IA dans sa propre base ({@code logiflow_ai}) :
 * Spring ne la stocke pas, il la relaie après authentification de l'utilisateur.
 */
public record ConversationCopilote(UUID id, String titre, Instant creeLe, Instant modifieLe) {

  public ConversationCopilote {
    Objects.requireNonNull(id, "L'identifiant de la conversation est obligatoire");
    Objects.requireNonNull(titre, "Le titre de la conversation est obligatoire");
  }
}
