package com.logiflow.tms.planning.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierPlanificationSummary;
import com.logiflow.tms.dossier.api.dto.SegmentPlanificationSummary;
import com.logiflow.tms.driver.api.ChauffeurApi;
import com.logiflow.tms.driver.api.dto.ChauffeurSummary;
import com.logiflow.tms.driver.api.dto.ExigencesAffectationDto;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorquePlanificationSummary;
import com.logiflow.tms.fleet.api.dto.VehiculePlanificationSummary;
import com.logiflow.tms.planning.application.ConformiteVoyageService.AnomalieConformite;
import com.logiflow.tms.planning.application.ConformiteVoyageService.RapportConformite;
import com.logiflow.tms.planning.application.DisponibiliteRessourcesService.RessourcesOccupees;
import com.logiflow.tms.planning.application.command.CreerVoyageCommand;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.TypeEtape;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Etape;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.referential.api.SiteApi;
import com.logiflow.tms.referential.api.dto.SiteSummary;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConformiteVoyageServiceTest {

  private static final Instant DEPART = Instant.parse("2026-10-05T06:00:00Z");
  private static final Instant ARRIVEE = DEPART.plus(10, ChronoUnit.HOURS);

  @Mock private DossierApi dossierApi;
  @Mock private VehiculeApi vehiculeApi;
  @Mock private RemorqueApi remorqueApi;
  @Mock private ChauffeurApi chauffeurApi;
  @Mock private SiteApi siteApi;
  @Mock private DisponibiliteRessourcesService disponibiliteRessourcesService;

  private ConformiteVoyageService service;

  private final UUID siteLyon = UUID.randomUUID();
  private final UUID siteMarseille = UUID.randomUUID();
  private final UUID vehiculeId = UUID.randomUUID();
  private final UUID remorqueId = UUID.randomUUID();
  private final UUID chauffeurId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    service =
        new ConformiteVoyageService(
            dossierApi,
            vehiculeApi,
            remorqueApi,
            chauffeurApi,
            siteApi,
            disponibiliteRessourcesService,
            new ItineraireDossiersDomainService(),
            new CapaciteTronconDomainService());
    lenient()
        .when(siteApi.consulterTous(anyCollection()))
        .thenReturn(
            List.of(
                new SiteSummary(siteLyon, "LYS", "Lyon", 45.76, 4.84, true),
                new SiteSummary(siteMarseille, "MRS", "Marseille", 43.30, 5.37, true)));
    lenient()
        .when(disponibiliteRessourcesService.ressourcesOccupees(DEPART, ARRIVEE, null))
        .thenReturn(new RessourcesOccupees(Map.of(), Map.of(), Map.of()));
    lenient()
        .when(chauffeurApi.consulter(chauffeurId))
        .thenReturn(
            Optional.of(
                new ChauffeurSummary(
                    chauffeurId, "DRV-1", "Martin", "Jean", "ACTIF", "DISPONIBLE", 2400)));
    lenient().when(chauffeurApi.motifsNonAffectation(eq(chauffeurId), any())).thenReturn(List.of());
    lenient().when(chauffeurApi.documentsValides(eq(chauffeurId), any())).thenReturn(true);
  }

  private DossierPlanificationSummary dossier(
      String reference, double poidsKg, String carrosserie, Double temperature) {
    return new DossierPlanificationSummary(
        UUID.randomUUID(),
        reference,
        "CREE",
        "NATIONAL",
        true,
        poidsKg,
        20,
        10,
        false,
        carrosserie,
        temperature,
        List.of(
            new SegmentPlanificationSummary(
                "CHARGEMENT", 0, siteLyon, DEPART, DEPART.plus(2, ChronoUnit.HOURS)),
            new SegmentPlanificationSummary(
                "DECHARGEMENT",
                1,
                siteMarseille,
                DEPART.plus(5, ChronoUnit.HOURS),
                DEPART.plus(8, ChronoUnit.HOURS))));
  }

  private void vehicule(String type) {
    when(vehiculeApi.consulterPourPlanification(eq(vehiculeId), any()))
        .thenReturn(
            Optional.of(
                new VehiculePlanificationSummary(
                    vehiculeId,
                    "AB-123-CD",
                    type,
                    9000,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    "DISPONIBLE",
                    true)));
  }

  private void remorque(String carrosserie, boolean froid, double chargeKg) {
    when(remorqueApi.consulterPourPlanification(eq(remorqueId), any()))
        .thenReturn(
            Optional.of(
                new RemorquePlanificationSummary(
                    remorqueId,
                    "RM-001-AA",
                    "SEMI_REMORQUE",
                    carrosserie,
                    chargeKg,
                    80,
                    33,
                    froid,
                    froid ? -20d : null,
                    froid ? 12d : null,
                    "DISPONIBLE",
                    true)));
  }

  private CreerVoyageCommand commande(List<DossierPlanificationSummary> dossiers, UUID remorque) {
    when(dossierApi.consulterPourPlanification(any())).thenReturn(dossiers);
    Trajet trajet =
        new Trajet(
            315,
            240,
            420,
            List.of(
                new Etape(0, TypeEtape.CHARGEMENT, DEPART, DEPART, 0, 0),
                new Etape(1, TypeEtape.DECHARGEMENT, ARRIVEE, null, 315, 0)));
    return new CreerVoyageCommand(
        dossiers.size() > 1 ? TypeVoyage.GROUPAGE : TypeVoyage.SIMPLE,
        Portee.NATIONAL,
        DEPART,
        ARRIVEE,
        vehiculeId,
        remorque,
        dossiers.stream().map(DossierPlanificationSummary::id).toList(),
        trajet,
        List.of(new Affectation(chauffeurId, RoleChauffeur.TITULAIRE, DEPART)));
  }

  private static List<String> codesBloquants(RapportConformite rapport) {
    return rapport.anomalies().stream()
        .filter(AnomalieConformite::bloquante)
        .map(AnomalieConformite::code)
        .toList();
  }

  @Test
  void voyageConformeConstruitLesArretsEtLaCapacite() {
    vehicule("TRACTEUR");
    remorque("TAUTLINER", false, 24000);
    DossierPlanificationSummary d1 = dossier("DT-1", 6000, "TAUTLINER", null);
    DossierPlanificationSummary d2 = dossier("DT-2", 4000, null, null);

    RapportConformite rapport =
        service.evaluer(commande(List.of(d1, d2), remorqueId), UUID.randomUUID(), null);

    assertThat(rapport.conforme()).as(rapport.anomalies().toString()).isTrue();
    assertThat(rapport.arrets())
        .extracting(a -> a.siteId())
        .containsExactly(siteLyon, siteMarseille);
    assertThat(rapport.arretsParDossier()).containsKeys(d1.id(), d2.id());
    assertThat(rapport.chargeMaxKg()).isEqualTo(10000);
    // Volume limitant : 2 × 20 m³ sur 80 m³ (le poids ne remplit que 10 t / 24 t).
    assertThat(rapport.tauxRemplissage()).isEqualTo(0.5);
  }

  @Test
  void tracteurSansRemorqueEstRefuseEtLePermisCeEstExige() {
    vehicule("TRACTEUR");

    RapportConformite rapport =
        service.evaluer(
            commande(List.of(dossier("DT-1", 1000, null, null)), null), UUID.randomUUID(), null);

    assertThat(codesBloquants(rapport)).contains("REMORQUE");
    ArgumentCaptor<ExigencesAffectationDto> exigences =
        ArgumentCaptor.forClass(ExigencesAffectationDto.class);
    verify(chauffeurApi).motifsNonAffectation(eq(chauffeurId), exigences.capture());
    assertThat(exigences.getValue().permisRequis()).isEqualTo("CE");
    assertThat(exigences.getValue().date()).isEqualTo(java.time.LocalDate.of(2026, 10, 5));
  }

  @Test
  void ressourcesDejaEngageesSurLaPeriodeSontRefusees() {
    vehicule("PORTEUR");
    when(disponibiliteRessourcesService.ressourcesOccupees(DEPART, ARRIVEE, null))
        .thenReturn(
            new RessourcesOccupees(
                Map.of(vehiculeId, "VOY-2026-000001"),
                Map.of(),
                Map.of(chauffeurId, "VOY-2026-000001")));

    RapportConformite rapport =
        service.evaluer(
            commande(List.of(dossier("DT-1", 1000, null, null)), null), UUID.randomUUID(), null);

    assertThat(rapport.anomalies())
        .filteredOn(a -> a.code().equals("CHEVAUCHEMENT"))
        .hasSize(2)
        .allMatch(a -> a.message().contains("VOY-2026-000001"));
  }

  @Test
  void carrosserieEtTemperatureIncompatiblesSontRefusees() {
    vehicule("TRACTEUR");
    remorque("TAUTLINER", false, 24000);

    RapportConformite rapport =
        service.evaluer(
            commande(List.of(dossier("DT-F", 3000, "FRIGORIFIQUE", 4d)), remorqueId),
            UUID.randomUUID(),
            null);

    assertThat(codesBloquants(rapport)).contains("CARROSSERIE", "TEMPERATURE");
  }

  @Test
  void capaciteDepasseeEstUneAnomalieMetier() {
    vehicule("TRACTEUR");
    remorque("TAUTLINER", false, 5000);

    RapportConformite rapport =
        service.evaluer(
            commande(List.of(dossier("DT-1", 6000, null, null)), remorqueId),
            UUID.randomUUID(),
            null);

    assertThat(codesBloquants(rapport)).containsExactly("CAPACITE");
  }

  @Test
  void motifsDuChauffeurEtSiteInconnuSontBloquants() {
    vehicule("PORTEUR");
    when(chauffeurApi.motifsNonAffectation(eq(chauffeurId), any()))
        .thenReturn(List.of("Chauffeur DRV-1 : statut SUSPENDU"));
    when(siteApi.consulterTous(anyCollection()))
        .thenReturn(List.of(new SiteSummary(siteLyon, "LYS", "Lyon", 45.76, 4.84, true)));

    RapportConformite rapport =
        service.evaluer(
            commande(List.of(dossier("DT-1", 1000, null, null)), null), UUID.randomUUID(), null);

    assertThat(rapport.messagesBloquants())
        .contains("Chauffeur DRV-1 : statut SUSPENDU")
        .anyMatch(m -> m.contains("déchargement du dossier DT-1 est introuvable"));
  }

  @Test
  void fenetreHorsPeriodeNestQuUnAvertissement() {
    vehicule("PORTEUR");
    DossierPlanificationSummary tardif =
        new DossierPlanificationSummary(
            UUID.randomUUID(),
            "DT-T",
            "CREE",
            "NATIONAL",
            true,
            1000,
            2,
            1,
            false,
            null,
            null,
            List.of(
                new SegmentPlanificationSummary(
                    "CHARGEMENT", 0, siteLyon, DEPART, DEPART.plus(1, ChronoUnit.HOURS)),
                new SegmentPlanificationSummary(
                    "DECHARGEMENT",
                    1,
                    siteMarseille,
                    DEPART.plus(3, ChronoUnit.DAYS),
                    DEPART.plus(3, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS))));

    RapportConformite rapport =
        service.evaluer(commande(List.of(tardif), null), UUID.randomUUID(), null);

    assertThat(rapport.conforme()).isTrue();
    assertThat(rapport.anomalies()).extracting(AnomalieConformite::code).contains("FENETRE");
  }
}
