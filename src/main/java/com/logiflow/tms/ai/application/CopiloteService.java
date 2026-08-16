package com.logiflow.tms.ai.application;

import com.logiflow.tms.ai.application.command.PoserQuestionCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.ReponseCopilote;
import com.logiflow.tms.ai.domain.model.TypeInteractionIa;
import com.logiflow.tms.ai.domain.port.out.AiServiceClientPort;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cas d'utilisation applicatif du copilote conversationnel. Aucun repli déterministe pertinent
 * n'existe pour une question ouverte en langage naturel : en cas d'indisponibilité du service IA,
 * l'échec est journalisé puis propagé (HTTP 503 côté contrôleur).
 */
@Service
@RequiredArgsConstructor
public class CopiloteService {

  private final AiServiceClientPort aiServiceClientPort;
  private final InteractionIaRepository interactionRepository;

  @Transactional
  public ReponseCopilote poserQuestion(PoserQuestionCommand command) {
    Instant debut = Instant.now();
    try {
      ReponseCopilote reponse =
          aiServiceClientPort.poserQuestion(
              command.question(), command.utilisateurId(), command.roles());
      journaliser(command.utilisateurId(), true, debut, command.question(), null);
      return reponse;
    } catch (ServiceIndisponibleException e) {
      journaliser(command.utilisateurId(), false, debut, command.question(), e.getMessage());
      throw e;
    }
  }

  private void journaliser(
      String utilisateurId, boolean succes, Instant debut, String resume, String erreur) {
    long dureeMs = Duration.between(debut, Instant.now()).toMillis();
    interactionRepository.sauvegarder(
        InteractionIa.enregistrer(
            UUID.randomUUID(),
            TypeInteractionIa.COPILOTE,
            utilisateurId,
            succes,
            dureeMs,
            resume,
            erreur));
  }
}
