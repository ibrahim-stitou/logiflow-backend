package com.logiflow.tms.ai.infrastructure.web.dto;

import com.logiflow.tms.ai.domain.model.ReponseCopilote;
import java.util.List;

public record CopiloteResponse(String reponse, List<String> sources, Double confiance) {

  public static CopiloteResponse depuis(ReponseCopilote reponse) {
    return new CopiloteResponse(reponse.reponse(), reponse.sources(), reponse.confiance());
  }
}
