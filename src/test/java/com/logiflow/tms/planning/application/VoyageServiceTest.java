package com.logiflow.tms.planning.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.planning.application.ConformiteVoyageService.AnomalieConformite;
import com.logiflow.tms.planning.application.ConformiteVoyageService.RapportConformite;
import com.logiflow.tms.planning.application.command.CreerVoyageCommand;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.TypeEtape;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService.ArretsDossier;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Etape;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VoyageServiceTest {

  @Mock private VoyageRepository voyageRepository;
  @Mock private SequenceReferenceGenerator referenceGenerator;
  @Mock private ConformiteVoyageService conformiteVoyageService;
  @Mock private VoyageArretRepository voyageArretRepository;
  @Mock private DossierApi dossierApi;
  @Mock private VoyageArretMaintenanceService voyageArretMaintenanceService;

  private VoyageService voyageService;

  @BeforeEach
  void setUp() {
    voyageService =
        new VoyageService(
            voyageRepository,
            referenceGenerator,
            conformiteVoyageService,
            voyageArretRepository,
            dossierApi,
            voyageArretMaintenanceService);
  }

  private CreerVoyageCommand commandeType(UUID dossierId) {
    Instant depart = Instant.now();
    Trajet trajet =
        new Trajet(
            450,
            360,
            420,
            List.of(
                new Etape(
                    0, TypeEtape.CHARGEMENT, depart, depart.plus(1, ChronoUnit.HOURS), 0, 500),
                new Etape(
                    1, TypeEtape.DECHARGEMENT, depart.plus(7, ChronoUnit.HOURS), null, 450, 0)));
    return new CreerVoyageCommand(
        TypeVoyage.SIMPLE,
        Portee.NATIONAL,
        depart,
        depart.plus(8, ChronoUnit.HOURS),
        UUID.randomUUID(),
        null,
        List.of(dossierId),
        trajet,
        List.of(new Affectation(UUID.randomUUID(), RoleChauffeur.TITULAIRE, depart)));
  }

  private static RapportConformite rapport(
      List<AnomalieConformite> anomalies,
      List<ArretVoyage> arrets,
      Map<UUID, ArretsDossier> arretsParDossier) {
    return new RapportConformite(
        anomalies, arrets, arretsParDossier, 0.4, 500, 2.5, 10, 9000d, null, null);
  }

  @Test
  void creerVoyageEchoueAvecLesMotifsBloquantsSansRienEcrire() {
    UUID dossierId = UUID.randomUUID();
    when(conformiteVoyageService.evaluer(any(), any(), eq(null)))
        .thenReturn(
            rapport(
                List.of(
                    new AnomalieConformite("VEHICULE", "Le véhicule n'est pas disponible", true),
                    new AnomalieConformite("FENETRE", "Fenêtre hors période", false)),
                List.of(),
                Map.of()));

    assertThatThrownBy(() -> voyageService.creerVoyage(commandeType(dossierId)))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e ->
                assertThat(((ValidationException) e).violations())
                    .containsExactly("Le véhicule n'est pas disponible"));
    verify(voyageRepository, never()).sauvegarder(any());
    verifyNoInteractions(voyageArretRepository, dossierApi);
  }

  @Test
  void creerVoyageConformePersisteLesArretsEtPlanifieLesDossiers() {
    UUID dossierId = UUID.randomUUID();
    UUID arretA = UUID.randomUUID();
    UUID arretB = UUID.randomUUID();
    when(conformiteVoyageService.evaluer(any(), any(), eq(null)))
        .thenAnswer(
            invocation -> {
              UUID voyageId = invocation.getArgument(1);
              List<ArretVoyage> arrets =
                  List.of(
                      ArretVoyage.creer(
                          arretA, voyageId, 0, "A", new GeoPoint(45, 4), UUID.randomUUID(), true),
                      ArretVoyage.creer(
                          arretB, voyageId, 1, "B", new GeoPoint(46, 5), UUID.randomUUID(), true));
              return rapport(
                  List.of(), arrets, Map.of(dossierId, new ArretsDossier(arretA, arretB, 0, 1)));
            });
    when(referenceGenerator.generer(anyString(), anyInt()))
        .thenReturn(new Reference("VOY-2026-000001"));
    when(voyageRepository.sauvegarder(any(Voyage.class))).thenAnswer(i -> i.getArgument(0));

    UUID voyageId = voyageService.creerVoyage(commandeType(dossierId));

    assertThat(voyageId).isNotNull();
    verify(voyageArretRepository).sauvegarderTous(any());
    verify(dossierApi).planifierSurVoyageAvecArrets(dossierId, arretA, arretB);
  }
}
