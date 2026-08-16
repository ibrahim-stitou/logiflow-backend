package com.logiflow.tms.ai.infrastructure.web;

import com.logiflow.tms.ai.application.CopiloteService;
import com.logiflow.tms.ai.application.command.PoserQuestionCommand;
import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteRequest;
import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteResponse;
import com.logiflow.tms.shared.infrastructure.security.CurrentUser;
import com.logiflow.tms.shared.infrastructure.security.SecurityContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Façade REST du copilote conversationnel, seul point d'accès exposé au frontend Angular — la
 * question est relayée en interne au service IA (Flask) via {@link CopiloteService}. Voir
 * docs/integration-ia.md.
 */
@RestController
@RequiredArgsConstructor
public class CopiloteController {

  private final CopiloteService copiloteService;
  private final SecurityContextService securityContextService;

  @PostMapping("/api/v1/ia/copilote/questions")
  public CopiloteResponse poser(@Valid @RequestBody CopiloteRequest request) {
    CurrentUser utilisateur =
        securityContextService
            .utilisateurCourant()
            .orElseThrow(() -> new AccessDeniedException("Authentification requise"));
    var reponse =
        copiloteService.poserQuestion(
            new PoserQuestionCommand(request.question(), utilisateur.sujet(), utilisateur.roles()));
    return CopiloteResponse.depuis(reponse);
  }
}
