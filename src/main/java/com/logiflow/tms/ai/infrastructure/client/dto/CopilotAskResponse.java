package com.logiflow.tms.ai.infrastructure.client.dto;

import java.util.List;

/** Corps de la réponse de Flask à POST /internal/ai/v1/copilot/ask. Voir docs/integration-ia.md. */
public record CopilotAskResponse(String reponse, List<String> sources, Double confiance) {}
