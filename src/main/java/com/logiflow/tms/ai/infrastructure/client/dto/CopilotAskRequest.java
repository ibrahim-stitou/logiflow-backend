package com.logiflow.tms.ai.infrastructure.client.dto;

import java.util.Set;

/**
 * Corps de la requête POST /internal/ai/v1/copilot/ask envoyée à Flask. Voir
 * docs/integration-ia.md.
 */
public record CopilotAskRequest(String question, UtilisateurDto utilisateur, String correlationId) {

  public record UtilisateurDto(String id, Set<String> roles) {}
}
