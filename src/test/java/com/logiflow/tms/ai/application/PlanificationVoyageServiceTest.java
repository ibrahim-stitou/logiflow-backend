package com.logiflow.tms.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.ai.application.command.ProposerVoyagesCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.planification.ContextePlanification;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.ai.domain.port.out.PlanificationClientPort;
import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierPlanificationSummary;
import com.logiflow.tms.dossier.api.dto.SegmentPlanificationSummary;
import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.driver.api.dto.ChauffeurPlanificationSummary;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.VehiculePlanificationSummary;
import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.planning.api.dto.RessourcesOccupeesSummary;
import com.logiflow.tms.referential.api.SiteApi;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanificationVoyageServiceTest {

  private static final Instant DEBUT = Instant.parse("2026-10-05T00:00:00Z");
  private static final Instant FIN = DEBUT.plus(2, ChronoUnit.DAYS);

  @Mock private PlanificationClientPort planificationClientPort;
  @Mock private InteractionIaRepository interactionRepository;
  @Mock private DossierApi dossierApi;
  @Mock private SiteApi siteApi;
  @Mock private VehiculeApi vehiculeApi;
  @Mock private RemorqueApi remorqueApi;
  @Mock private ChauffeurApi chauffeurApi;
  @Mock private VoyageApi voyageApi;
  @InjectMocks private PlanificationVoyageService service;

  private static DossierPlanificationSummary dossier(String ref, boolean international) {
    UUID site = UUID.randomUUID();
    return new DossierPlanificationSummary(
        UUID.randomUUID(),
        ref,
        "CREE",
        international ? "EXPORT" : "NATIONAL",
        true,
        1000,
        2,
        1,
        false,
        null,
        null,
        List.of(
            new SegmentPlanificationSummary("CHARGEMENT", 0, site, DEBUT, DEBUT.plusSeconds(3600)),
            new SegmentPlanificationSummary(
                "DECHARGEMENT", 1, UUID.randomUUID(), DEBUT, DEBUT.plusSeconds(7200))));
  }

  private static VehiculePlanificationSummary vehicule(String statut, boolean documents) {
    return new VehiculePlanificationSummary(
        UUID.randomUUID(),
        "AB-1",
        "PORTEUR",
        9000,
        null,
        null,
        null,
        false,
        null,
        null,
        statut,
        documents);
  }

  private static ChauffeurPlanificationSummary chauffeur(List<String> motifs) {
    return new ChauffeurPlanificationSummary(
        UUID.randomUUID(),
        "DRV",
        "Nom",
        "Prenom",
        "ACTIF",
        "DISPONIBLE",
        2000,
        List.of(),
        List.of(),
        false,
        null,
        motifs);
  }

  @Test
  void neTransmetQueLesDossiersDeLaPorteeEtLesRessourcesLibres() {
    DossierPlanificationSummary national = dossier("DT-N", false);
    when(dossierApi.candidatsPlanification(DEBUT, FIN))
        .thenReturn(List.of(national, dossier("DT-X", true)));
    VehiculePlanificationSummary libre = vehicule("DISPONIBLE", true);
    VehiculePlanificationSummary occupe = vehicule("EN_VOYAGE", true);
    when(vehiculeApi.listerPourPlanification(any()))
        .thenReturn(
            List.of(
                libre, occupe, vehicule("EN_MAINTENANCE", true), vehicule("DISPONIBLE", false)));
    when(remorqueApi.listerPourPlanification(any())).thenReturn(List.of());
    ChauffeurPlanificationSummary affectable = chauffeur(List.of());
    when(chauffeurApi.listerPourPlanification(any()))
        .thenReturn(List.of(affectable, chauffeur(List.of("Chauffeur DRV : statut SUSPENDU"))));
    when(voyageApi.ressourcesOccupees(DEBUT, FIN))
        .thenReturn(new RessourcesOccupeesSummary(Set.of(occupe.id()), Set.of(), Set.of()));
    when(planificationClientPort.proposer(any()))
        .thenThrow(new ServiceIndisponibleException("indisponible"));

    assertThatThrownBy(
            () ->
                service.proposer(new ProposerVoyagesCommand(DEBUT, FIN, "GROUPAGE", "NATIONAL", 3)))
        .isInstanceOf(ServiceIndisponibleException.class);

    ArgumentCaptor<ContextePlanification> contexte =
        ArgumentCaptor.forClass(ContextePlanification.class);
    verify(planificationClientPort).proposer(contexte.capture());
    assertThat(contexte.getValue().dossiers())
        .extracting(ContextePlanification.Dossier::reference)
        .containsExactly("DT-N");
    assertThat(contexte.getValue().vehicules())
        .extracting(ContextePlanification.Vehicule::id)
        .containsExactly(libre.id().toString());
    assertThat(contexte.getValue().chauffeurs())
        .extracting(ContextePlanification.Chauffeur::id)
        .containsExactly(affectable.id().toString());

    ArgumentCaptor<InteractionIa> journal = ArgumentCaptor.forClass(InteractionIa.class);
    verify(interactionRepository).sauvegarder(journal.capture());
    assertThat(journal.getValue().succes()).isFalse();
  }

  @Test
  void periodeTropLongueEstRefuseeSansAppelerLAgent() {
    assertThatThrownBy(
            () ->
                service.proposer(
                    new ProposerVoyagesCommand(
                        DEBUT, DEBUT.plus(30, ChronoUnit.DAYS), "GROUPAGE", "NATIONAL", 3)))
        .isInstanceOf(BusinessException.class);
    verify(planificationClientPort, never()).proposer(any());
  }
}
