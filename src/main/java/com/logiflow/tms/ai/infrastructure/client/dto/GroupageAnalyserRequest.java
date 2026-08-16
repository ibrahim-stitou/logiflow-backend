package com.logiflow.tms.ai.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

/**
 * Corps de la requête POST /internal/ai/v1/groupage/analyser envoyée à Flask. Voir
 * docs/integration-ia.md.
 */
public record GroupageAnalyserRequest(List<DossierCandidatDto> dossiers, String correlationId) {

  public record DossierCandidatDto(
      UUID id,
      String reference,
      double poidsBrutKg,
      double volumeM3,
      int nbPalettes,
      boolean contientAdr) {}
}
