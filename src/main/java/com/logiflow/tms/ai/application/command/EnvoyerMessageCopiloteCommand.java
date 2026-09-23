package com.logiflow.tms.ai.application.command;

import java.util.Set;
import java.util.UUID;

/** Question posée dans une conversation du copilote. Rôles avec ou sans préfixe {@code ROLE_}. */
public record EnvoyerMessageCopiloteCommand(
    UUID conversationId,
    String question,
    String utilisateurId,
    String nomAffichage,
    Set<String> roles) {}
