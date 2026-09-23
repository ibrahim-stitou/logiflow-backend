package com.logiflow.tms.ai.infrastructure.client.dto;

import com.logiflow.tms.ai.domain.model.copilote.ConversationCopilote;
import com.logiflow.tms.ai.domain.model.copilote.ConversationCopiloteDetail;
import com.logiflow.tms.ai.domain.model.copilote.MessageCopilote;
import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Contrat JSON des conversations du copilote côté service IA (Flask). */
public final class CopiloteConversationDto {

  private CopiloteConversationDto() {}

  public record Conversation(UUID id, String titre, Instant createdAt, Instant updatedAt) {

    public ConversationCopilote versDomaine() {
      return new ConversationCopilote(id, titre, createdAt, updatedAt);
    }
  }

  public record Source(String type, String reference, String id) {}

  public record Message(
      UUID id,
      String role,
      String contenu,
      String statut,
      List<Source> sources,
      Instant createdAt) {

    MessageCopilote versDomaine() {
      return new MessageCopilote(
          id,
          role,
          contenu,
          statut,
          sources == null
              ? List.of()
              : sources.stream()
                  .map(s -> new SourceCopilote(s.type(), s.reference(), s.id()))
                  .toList(),
          createdAt);
    }
  }

  public record Detail(
      UUID id, String titre, Instant createdAt, Instant updatedAt, List<Message> messages) {

    public ConversationCopiloteDetail versDomaine() {
      return new ConversationCopiloteDetail(
          new ConversationCopilote(id, titre, createdAt, updatedAt),
          messages == null ? List.of() : messages.stream().map(Message::versDomaine).toList());
    }
  }

  public record Titre(String titre) {}

  public record Feedback(int note, String commentaire) {}

  public record EnvoyerMessage(
      String question, Utilisateur utilisateur, String contexte, String correlationId) {}

  public record Utilisateur(String id, String nom, List<String> roles) {}
}
