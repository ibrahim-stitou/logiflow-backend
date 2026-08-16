package com.logiflow.tms.ai.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

/**
 * Corps de la réponse de Flask à POST /internal/ai/v1/groupage/analyser. Voir
 * docs/integration-ia.md.
 */
public record GroupageAnalyserResponse(List<PropositionDto> propositions) {

  public record PropositionDto(
      List<UUID> dossierIds,
      Double score,
      Double confiance,
      Double gainKm,
      Double gainMarge,
      String justification) {}
}
