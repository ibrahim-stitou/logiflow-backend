package com.logiflow.tms.ai.infrastructure.web.dto;

import com.logiflow.tms.ai.domain.model.PropositionGroupage;
import java.util.List;
import java.util.UUID;

public record PropositionGroupageResponse(
    List<UUID> dossierIds,
    Double score,
    Double confiance,
    Double gainKm,
    Double gainMarge,
    String justification,
    boolean genereParIa) {

  public static PropositionGroupageResponse depuis(PropositionGroupage proposition) {
    return new PropositionGroupageResponse(
        proposition.dossierIds(),
        proposition.score(),
        proposition.confiance(),
        proposition.gainKm(),
        proposition.gainMarge(),
        proposition.justification(),
        proposition.genereParIa());
  }
}
