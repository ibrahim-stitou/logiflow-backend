package com.logiflow.tms.tracking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.planning.api.dto.VoyageSummary;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.tracking.application.command.DeclarerEvenementCommand;
import com.logiflow.tms.tracking.domain.model.EvenementVoyage;
import com.logiflow.tms.tracking.domain.model.TypeEvenement;
import com.logiflow.tms.tracking.domain.port.out.EvenementVoyageRepository;
import com.logiflow.tms.tracking.domain.service.TrackingDomainService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvenementVoyageServiceTest {

  @Mock private EvenementVoyageRepository evenementRepository;
  @Mock private VoyageApi voyageApi;

  private EvenementVoyageService evenementService;

  @BeforeEach
  void setUp() {
    evenementService =
        new EvenementVoyageService(evenementRepository, new TrackingDomainService(), voyageApi);
  }

  @Test
  void declarerUnEvenementEchoueSiLeVoyageEstIntrouvable() {
    UUID voyageId = UUID.randomUUID();
    when(voyageApi.consulter(voyageId)).thenReturn(Optional.empty());
    DeclarerEvenementCommand command =
        new DeclarerEvenementCommand(voyageId, TypeEvenement.DEPART, Instant.now(), null, null);

    assertThatThrownBy(() -> evenementService.declarerEvenement(command))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void declarerUnEvenementSauvegardeUnNouvelEvenement() {
    UUID voyageId = UUID.randomUUID();
    when(voyageApi.consulter(voyageId))
        .thenReturn(
            Optional.of(
                new VoyageSummary(
                    voyageId, "VOY-2026-000001", "BROUILLON", UUID.randomUUID(), List.of())));
    when(evenementRepository.dernierParVoyageId(voyageId)).thenReturn(Optional.empty());
    when(evenementRepository.sauvegarder(any(EvenementVoyage.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    DeclarerEvenementCommand command =
        new DeclarerEvenementCommand(voyageId, TypeEvenement.DEPART, Instant.now(), null, null);

    UUID id = evenementService.declarerEvenement(command);

    assertThat(id).isNotNull();
  }
}
