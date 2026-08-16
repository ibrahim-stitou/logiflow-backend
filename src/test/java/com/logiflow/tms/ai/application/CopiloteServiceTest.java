package com.logiflow.tms.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.ai.application.command.PoserQuestionCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.ReponseCopilote;
import com.logiflow.tms.ai.domain.port.out.AiServiceClientPort;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CopiloteServiceTest {

  @Mock private AiServiceClientPort aiServiceClientPort;
  @Mock private InteractionIaRepository interactionRepository;

  private CopiloteService copiloteService;

  @BeforeEach
  void setUp() {
    copiloteService = new CopiloteService(aiServiceClientPort, interactionRepository);
  }

  @Test
  void poserQuestionRenvoieLaReponseEtJournaliseLeSucces() {
    PoserQuestionCommand command =
        new PoserQuestionCommand(
            "Quels camions sont libres demain ?", "user-1", Set.of("EXPLOITANT"));
    when(aiServiceClientPort.poserQuestion(
            command.question(), command.utilisateurId(), command.roles()))
        .thenReturn(
            new ReponseCopilote("3 véhicules disponibles", List.of("vehicule:AB-123-CD"), 0.9));
    when(interactionRepository.sauvegarder(any(InteractionIa.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ReponseCopilote reponse = copiloteService.poserQuestion(command);

    assertThat(reponse.reponse()).isEqualTo("3 véhicules disponibles");
    verify(interactionRepository).sauvegarder(any(InteractionIa.class));
  }

  @Test
  void poserQuestionPropageEtJournaliseLIndisponibiliteDuServiceIa() {
    PoserQuestionCommand command =
        new PoserQuestionCommand("Question", "user-1", Set.of("EXPLOITANT"));
    when(aiServiceClientPort.poserQuestion(
            command.question(), command.utilisateurId(), command.roles()))
        .thenThrow(new ServiceIndisponibleException("indisponible"));
    when(interactionRepository.sauvegarder(any(InteractionIa.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    assertThatThrownBy(() -> copiloteService.poserQuestion(command))
        .isInstanceOf(ServiceIndisponibleException.class);
    verify(interactionRepository).sauvegarder(any(InteractionIa.class));
  }
}
