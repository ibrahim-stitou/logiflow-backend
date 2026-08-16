package com.logiflow.tms.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.logiflow.tms.ai.application.command.AnalyserGroupageCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.PropositionGroupage;
import com.logiflow.tms.ai.domain.port.out.AiServiceClientPort;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.ai.domain.service.AiDomainService;
import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierSummary;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupageAdvisorServiceTest {

  @Mock private AiServiceClientPort aiServiceClientPort;
  @Mock private InteractionIaRepository interactionRepository;
  @Mock private DossierApi dossierApi;

  private GroupageAdvisorService groupageAdvisorService;

  @BeforeEach
  void setUp() {
    groupageAdvisorService =
        new GroupageAdvisorService(
            aiServiceClientPort, interactionRepository, new AiDomainService(), dossierApi);
  }

  @Test
  void analyserGroupageRenvoieLesPropositionsDuServiceIaQuandDisponible() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    when(dossierApi.consulter(id1))
        .thenReturn(
            Optional.of(
                new DossierSummary(id1, "DT-2026-000001", "CREE", true, 500, 2.5, 10, false)));
    when(dossierApi.consulter(id2))
        .thenReturn(
            Optional.of(
                new DossierSummary(id2, "DT-2026-000002", "CREE", true, 300, 1.5, 5, false)));
    when(aiServiceClientPort.analyserGroupage(anyList()))
        .thenReturn(
            List.of(
                new PropositionGroupage(
                    List.of(id1, id2), 0.9, 0.8, 100.0, 200.0, "Compatibles", true)));
    when(interactionRepository.sauvegarder(any(InteractionIa.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    List<PropositionGroupage> propositions =
        groupageAdvisorService.analyserGroupage(new AnalyserGroupageCommand(List.of(id1, id2)));

    assertThat(propositions).hasSize(1);
    assertThat(propositions.get(0).genereParIa()).isTrue();
  }

  @Test
  void analyserGroupageSeReplieSurLeFiltrageDurQuandLeServiceIaEstIndisponible() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    when(dossierApi.consulter(id1))
        .thenReturn(
            Optional.of(
                new DossierSummary(id1, "DT-2026-000001", "CREE", true, 500, 2.5, 10, false)));
    when(dossierApi.consulter(id2))
        .thenReturn(
            Optional.of(
                new DossierSummary(id2, "DT-2026-000002", "CREE", true, 300, 1.5, 5, false)));
    when(aiServiceClientPort.analyserGroupage(anyList()))
        .thenThrow(new ServiceIndisponibleException("indisponible"));
    when(interactionRepository.sauvegarder(any(InteractionIa.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    List<PropositionGroupage> propositions =
        groupageAdvisorService.analyserGroupage(new AnalyserGroupageCommand(List.of(id1, id2)));

    assertThat(propositions).hasSize(1);
    assertThat(propositions.get(0).genereParIa()).isFalse();
    assertThat(propositions.get(0).dossierIds()).containsExactlyInAnyOrder(id1, id2);
  }
}
