package com.logiflow.tms.planning.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierSummary;
import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.driver.api.dto.ChauffeurSummary;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.planning.application.command.CreerVoyageCommand;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.TypeEtape;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.SequenceReferenceGenerator;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.service.ConformiteDomainService;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Etape;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
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
  @Mock private DossierApi dossierApi;
  @Mock private VehiculeApi vehiculeApi;
  @Mock private RemorqueApi remorqueApi;
  @Mock private ChauffeurApi chauffeurApi;

  private VoyageService voyageService;

  @BeforeEach
  void setUp() {
    voyageService =
        new VoyageService(
            voyageRepository,
            referenceGenerator,
            new ConformiteDomainService(),
            dossierApi,
            vehiculeApi,
            remorqueApi,
            chauffeurApi);
  }

  private CreerVoyageCommand commandeType(UUID dossierId, UUID vehiculeId, UUID chauffeurId) {
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
        vehiculeId,
        null,
        List.of(dossierId),
        trajet,
        List.of(new Affectation(chauffeurId, RoleChauffeur.TITULAIRE, depart)));
  }

  @Test
  void creerVoyageEchoueSiLeVehiculeNEstPasDisponible() {
    UUID dossierId = UUID.randomUUID();
    UUID vehiculeId = UUID.randomUUID();
    UUID chauffeurId = UUID.randomUUID();

    when(dossierApi.consulter(dossierId))
        .thenReturn(
            Optional.of(
                new DossierSummary(
                    dossierId, "DT-2026-000001", "CREE", true, 500, 2.5, 10, false)));
    when(vehiculeApi.estDisponible(vehiculeId)).thenReturn(false);
    lenient().when(vehiculeApi.documentsValides(any(), any())).thenReturn(true);
    lenient().when(chauffeurApi.estDisponible(chauffeurId)).thenReturn(true);
    lenient()
        .when(chauffeurApi.consulter(chauffeurId))
        .thenReturn(
            Optional.of(
                new ChauffeurSummary(
                    chauffeurId, "CH-001", "Dupont", "Jean", "ACTIF", "DISPONIBLE", 600)));

    assertThatThrownBy(
            () -> voyageService.creerVoyage(commandeType(dossierId, vehiculeId, chauffeurId)))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void creerVoyageSauvegardeUnNouveauVoyageQuandTousLesCriteresSontConformes() {
    UUID dossierId = UUID.randomUUID();
    UUID vehiculeId = UUID.randomUUID();
    UUID chauffeurId = UUID.randomUUID();

    when(dossierApi.consulter(dossierId))
        .thenReturn(
            Optional.of(
                new DossierSummary(
                    dossierId, "DT-2026-000001", "CREE", true, 500, 2.5, 10, false)));
    when(vehiculeApi.estDisponible(vehiculeId)).thenReturn(true);
    when(vehiculeApi.documentsValides(any(), any())).thenReturn(true);
    when(chauffeurApi.estDisponible(chauffeurId)).thenReturn(true);
    when(chauffeurApi.consulter(chauffeurId))
        .thenReturn(
            Optional.of(
                new ChauffeurSummary(
                    chauffeurId, "CH-001", "Dupont", "Jean", "ACTIF", "DISPONIBLE", 600)));
    when(referenceGenerator.generer(anyString(), anyInt()))
        .thenReturn(Reference.generer("VOY", 2026, 1));
    when(voyageRepository.sauvegarder(any(Voyage.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID id = voyageService.creerVoyage(commandeType(dossierId, vehiculeId, chauffeurId));

    assertThat(id).isNotNull();
    verify(dossierApi).planifierPourVoyage(List.of(dossierId));
  }
}
