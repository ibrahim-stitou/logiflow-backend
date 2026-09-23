package com.logiflow.tms.ai.domain.model.copilote;

import java.util.List;
import java.util.Objects;

/** Conversation et ses messages, dans l'ordre chronologique. */
public record ConversationCopiloteDetail(
    ConversationCopilote conversation, List<MessageCopilote> messages) {

  public ConversationCopiloteDetail {
    Objects.requireNonNull(conversation, "La conversation est obligatoire");
    messages = messages != null ? List.copyOf(messages) : List.of();
  }
}
