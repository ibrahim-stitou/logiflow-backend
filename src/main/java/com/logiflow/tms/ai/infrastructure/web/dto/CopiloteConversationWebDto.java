package com.logiflow.tms.ai.infrastructure.web.dto;

import com.logiflow.tms.ai.domain.model.copilote.ConversationCopilote;
import com.logiflow.tms.ai.domain.model.copilote.ConversationCopiloteDetail;
import com.logiflow.tms.ai.domain.model.copilote.EtatCopilote;
import com.logiflow.tms.ai.domain.model.copilote.MessageCopilote;
import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Contrat REST du copilote conversationnel exposé au frontend Angular. */
public final class CopiloteConversationWebDto {

  private CopiloteConversationWebDto() {}

  public record EtatResponse(
      boolean operationnel,
      boolean serviceIa,
      String llm,
      String base,
      String modele,
      String fournisseur) {

    public static EtatResponse depuis(EtatCopilote etat) {
      return new EtatResponse(
          etat.operationnel(),
          etat.serviceIa(),
          etat.llm(),
          etat.base(),
          etat.modele(),
          etat.fournisseur());
    }
  }

  public record CreerConversationRequest(@Size(max = 200) String titre) {}

  public record RenommerConversationRequest(@NotBlank @Size(max = 200) String titre) {}

  public record EnvoyerMessageRequest(@NotBlank @Size(max = 4000) String question) {}

  public record FeedbackRequest(
      @NotNull @Min(-1) @Max(1) Integer note, @Size(max = 2000) String commentaire) {}

  public record ConversationResponse(UUID id, String titre, Instant creeLe, Instant modifieLe) {

    public static ConversationResponse depuis(ConversationCopilote conversation) {
      return new ConversationResponse(
          conversation.id(), conversation.titre(), conversation.creeLe(), conversation.modifieLe());
    }
  }

  public record MessageResponse(
      UUID id,
      String role,
      String contenu,
      String statut,
      List<SourceCopilote> sources,
      Instant creeLe) {

    static MessageResponse depuis(MessageCopilote message) {
      return new MessageResponse(
          message.id(),
          message.role(),
          message.contenu(),
          message.statut(),
          message.sources(),
          message.creeLe());
    }
  }

  public record ConversationDetailResponse(
      UUID id, String titre, Instant creeLe, Instant modifieLe, List<MessageResponse> messages) {

    public static ConversationDetailResponse depuis(ConversationCopiloteDetail detail) {
      var conversation = detail.conversation();
      return new ConversationDetailResponse(
          conversation.id(),
          conversation.titre(),
          conversation.creeLe(),
          conversation.modifieLe(),
          detail.messages().stream().map(MessageResponse::depuis).toList());
    }
  }
}
