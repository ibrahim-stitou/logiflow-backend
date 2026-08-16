package com.logiflow.tms.ai.infrastructure.web;

import com.logiflow.tms.ai.application.GroupageAdvisorService;
import com.logiflow.tms.ai.application.command.AnalyserGroupageCommand;
import com.logiflow.tms.ai.infrastructure.web.dto.GroupageAnalyseRequest;
import com.logiflow.tms.ai.infrastructure.web.dto.PropositionGroupageResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Façade REST de l'agent de groupage, seul point d'accès exposé au frontend Angular. En cas
 * d'indisponibilité du service IA, renvoie un repli déterministe plutôt qu'une erreur — voir {@link
 * GroupageAdvisorService} et docs/integration-ia.md.
 */
@RestController
@RequiredArgsConstructor
public class GroupageAdvisorController {

  private final GroupageAdvisorService groupageAdvisorService;

  @PostMapping("/api/v1/ia/groupage/propositions")
  public List<PropositionGroupageResponse> analyser(
      @Valid @RequestBody GroupageAnalyseRequest request) {
    return groupageAdvisorService
        .analyserGroupage(new AnalyserGroupageCommand(request.dossierIds()))
        .stream()
        .map(PropositionGroupageResponse::depuis)
        .toList();
  }
}
