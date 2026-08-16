package com.logiflow.tms.ai.application.command;

import java.util.Set;

/** Commande applicative de question posée au copilote conversationnel. */
public record PoserQuestionCommand(String question, String utilisateurId, Set<String> roles) {}
