package com.logiflow.tms.ai.infrastructure.web;

import com.logiflow.tms.ai.application.CopiloteConversationService;
import com.logiflow.tms.ai.application.command.EnvoyerMessageCopiloteCommand;
import com.logiflow.tms.ai.domain.model.copilote.EvenementCopilote;
import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteConversationWebDto.ConversationDetailResponse;
import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteConversationWebDto.ConversationResponse;
import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteConversationWebDto.CreerConversationRequest;
import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteConversationWebDto.EnvoyerMessageRequest;
import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteConversationWebDto.FeedbackRequest;
import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteConversationWebDto.RenommerConversationRequest;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import com.logiflow.tms.shared.infrastructure.security.CurrentUser;
import com.logiflow.tms.shared.infrastructure.security.SecurityContextService;
import com.logiflow.tms.shared.infrastructure.web.CorrelationIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Façade REST du copilote conversationnel (chatbot), seul point d'accès du frontend : les
 * conversations sont hébergées par le service IA, joint en interne. La réponse à un message est un
 * flux {@code text/event-stream} relayé événement par événement (voir docs/integration-ia.md).
 */
@RestController
@Validated
@RequestMapping("/api/v1/ia/copilote")
public class CopiloteConversationController {

  private static final Logger log = LoggerFactory.getLogger(CopiloteConversationController.class);

  /**
   * Au-delà, le flux est clos côté Spring même si le service IA n'a pas terminé. Large : sur CPU,
   * un LLM local peut mettre plusieurs minutes pour une question nécessitant des outils.
   */
  private static final long DUREE_MAX_FLUX_MS = 900_000;

  private final CopiloteConversationService conversationService;
  private final SecurityContextService securityContextService;
  private final Executor executor;

  public CopiloteConversationController(
      CopiloteConversationService conversationService,
      SecurityContextService securityContextService,
      @Qualifier("applicationTaskExecutor") Executor executor) {
    this.conversationService = conversationService;
    this.securityContextService = securityContextService;
    this.executor = executor;
  }

  @GetMapping("/conversations")
  public List<ConversationResponse> lister(
      @RequestParam(defaultValue = "30") @Min(1) @Max(100) int limite,
      @RequestParam(defaultValue = "0") @Min(0) int decalage) {
    return conversationService.lister(utilisateur().sujet(), limite, decalage).stream()
        .map(ConversationResponse::depuis)
        .toList();
  }

  @PostMapping("/conversations")
  @ResponseStatus(HttpStatus.CREATED)
  public ConversationResponse creer(
      @Valid @RequestBody(required = false) CreerConversationRequest request) {
    String titre = request != null ? request.titre() : null;
    return ConversationResponse.depuis(conversationService.creer(utilisateur().sujet(), titre));
  }

  @GetMapping("/conversations/{id}")
  public ConversationDetailResponse obtenir(@PathVariable UUID id) {
    return ConversationDetailResponse.depuis(
        conversationService.obtenir(id, utilisateur().sujet()));
  }

  @PatchMapping("/conversations/{id}")
  public ConversationResponse renommer(
      @PathVariable UUID id, @Valid @RequestBody RenommerConversationRequest request) {
    return ConversationResponse.depuis(
        conversationService.renommer(id, utilisateur().sujet(), request.titre()));
  }

  @DeleteMapping("/conversations/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void supprimer(@PathVariable UUID id) {
    conversationService.supprimer(id, utilisateur().sujet());
  }

  @PostMapping("/messages/{id}/feedback")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void noter(@PathVariable UUID id, @Valid @RequestBody FeedbackRequest request) {
    if (request.note() == 0) {
      throw new ValidationException("La note doit valoir -1 ou 1.", List.of("note: -1 ou 1"));
    }
    conversationService.noter(id, utilisateur().sujet(), request.note(), request.commentaire());
  }

  @PostMapping(path = "/conversations/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter envoyerMessage(
      @PathVariable UUID id, @Valid @RequestBody EnvoyerMessageRequest request) {
    CurrentUser utilisateur = utilisateur();
    var command =
        new EnvoyerMessageCopiloteCommand(
            id,
            request.question(),
            utilisateur.sujet(),
            utilisateur.nomAffichage(),
            utilisateur.roles());
    SseEmitter emetteur = new SseEmitter(DUREE_MAX_FLUX_MS);
    String correlationId = CorrelationIdFilter.correlationIdCourant();
    executor.execute(
        () -> {
          if (correlationId != null) {
            MDC.put(CorrelationIdFilter.MDC_KEY, correlationId);
          }
          try {
            conversationService.envoyerMessage(command, evenement -> emettre(emetteur, evenement));
            emetteur.complete();
          } catch (RuntimeException e) {
            // Le plus souvent : le client a fermé la connexion (bouton Stop, onglet fermé).
            log.debug("Flux du copilote interrompu pour la conversation {}", id, e);
            emetteur.completeWithError(e);
          } finally {
            MDC.clear();
          }
        });
    return emetteur;
  }

  private static void emettre(SseEmitter emetteur, EvenementCopilote evenement) {
    try {
      emetteur.send(
          SseEmitter.event().name(evenement.nom()).data(evenement.donnees(), MediaType.TEXT_PLAIN));
    } catch (IOException | IllegalStateException e) {
      throw new UncheckedIOException(
          e instanceof IOException io ? io : new IOException("Flux SSE fermé", e));
    }
  }

  private CurrentUser utilisateur() {
    return securityContextService
        .utilisateurCourant()
        .orElseThrow(() -> new AccessDeniedException("Authentification requise"));
  }
}
