package com.logiflow.tms.dossier.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.logiflow.tms.dossier.application.command.CreerDossierCommand;
import com.logiflow.tms.dossier.domain.model.DossierTransport;
import com.logiflow.tms.dossier.domain.model.TypeSegment;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.port.out.DossierTransportRepository;
import com.logiflow.tms.dossier.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import com.logiflow.tms.order.api.CommandeApi;
import com.logiflow.tms.referential.api.MarchandiseApi;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Reference;
import com.logiflow.tms.shared.domain.vo.TimeWindow;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DossierTransportServiceTest {

  @Mock private DossierTransportRepository dossierRepository;
  @Mock private SequenceReferenceGenerator referenceGenerator;
  @Mock private CommandeApi commandeApi;
  @Mock private MarchandiseApi marchandiseApi;

  private DossierTransportService dossierService;

  @BeforeEach
  void setUp() {
    dossierService =
        new DossierTransportService(dossierRepository, referenceGenerator, commandeApi, marchandiseApi);
  }

  private CreerDossierCommand commandeType(UUID commandeId) {
    Instant maintenant = Instant.now();
    return new CreerDossierCommand(
        commandeId,
        TypeTransport.NATIONAL,
        true,
        10,
        "Palettes",
        null,
        null,
        List.of(new LigneMarchandise(UUID.randomUUID(), 500, 2.5, 10, null, null, true)),
        List.of(
            new Segment(
                TypeSegment.CHARGEMENT,
                0,
                UUID.randomUUID(),
                new TimeWindow(maintenant, maintenant.plus(2, ChronoUnit.HOURS)),
                null),
            new Segment(
                TypeSegment.DECHARGEMENT,
                1,
                UUID.randomUUID(),
                new TimeWindow(
                    maintenant.plus(1, ChronoUnit.DAYS),
                    maintenant.plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)),
                null)),
        List.of());
  }

  @Test
  void creerDossierEchoueSiLaCommandeNEstPasConfirmee() {
    UUID commandeId = UUID.randomUUID();
    when(commandeApi.estConfirmee(commandeId)).thenReturn(false);

    assertThatThrownBy(() -> dossierService.creerDossier(commandeType(commandeId)))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void creerDossierSauvegardeUnNouveauDossier() {
    UUID commandeId = UUID.randomUUID();
    when(commandeApi.estConfirmee(commandeId)).thenReturn(true);
    when(marchandiseApi.estActif(any())).thenReturn(true);
    when(referenceGenerator.generer(anyString(), anyInt()))
        .thenReturn(Reference.generer("DT", 2026, 1));
    when(dossierRepository.sauvegarder(any(DossierTransport.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID id = dossierService.creerDossier(commandeType(commandeId));

    assertThat(id).isNotNull();
  }
}
