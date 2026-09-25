package com.logiflow.tms.ai.infrastructure.web;

import com.logiflow.tms.ai.application.PlanificationVoyageService;
import com.logiflow.tms.ai.infrastructure.web.dto.PlanificationRequest;
import com.logiflow.tms.ai.infrastructure.web.dto.PropositionsVoyageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Façade REST de l'agent de planification de voyage. Si le service IA est indisponible, répond 503
 * (voir {@code GlobalExceptionHandler}) : l'écran bascule alors sur la planification manuelle.
 */
@RestController
@RequiredArgsConstructor
public class PlanificationController {

  private final PlanificationVoyageService planificationVoyageService;

  @PostMapping("/api/v1/ia/planification/propositions")
  public PropositionsVoyageResponse proposer(@Valid @RequestBody PlanificationRequest request) {
    return PropositionsVoyageResponse.depuis(
        planificationVoyageService.proposer(request.versCommande()));
  }
}
