package com.logiflow.tms.planning.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierCapaciteSummary;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorqueSummary;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.TypeEtape;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.ResultatVerification;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.UtilisationTroncon;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Etape;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
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
class VoyageCapaciteServiceTest {

  @Mock private VoyageRepository voyageRepository;
  @Mock private VoyageArretRepository voyageArretRepository;
  @Mock private DossierApi dossierApi;
  @Mock private RemorqueApi remorqueApi;
  @Mock private VehiculeApi vehiculeApi;

  private VoyageCapaciteService voyageCapaciteService;

  private UUID voyageId;
  private UUID remorqueId;
  private UUID dossierId;
  private UUID arretDepart;
  private UUID arretMilieu;
  private UUID arretFin;

  @BeforeEach
  void setUp() {
    voyageCapaciteService =
        new VoyageCapaciteService(
            voyageRepository,
            voyageArretRepository,
            dossierApi,
            remorqueApi,
            vehiculeApi,
            new CapaciteTronconDomainService());

    voyageId = UUID.randomUUID();
    remorqueId = UUID.randomUUID();
    dossierId = UUID.randomUUID();
    arretDepart = UUID.randomUUID();
    arretMilieu = UUID.randomUUID();
    arretFin = UUID.randomUUID();
  }

  @Test
  void obtenirUtilisationParTronconRetourneLesTotauxParLeg() {
    preparerContexteVoyage(500, 2.5, arretDepart, arretMilieu);

    List<UtilisationTroncon> utilisation =
        voyageCapaciteService.obtenirUtilisationParTroncon(voyageId);

    assertThat(utilisation).hasSize(2);
    assertThat(utilisation.get(0).poidsUtiliseKg()).isEqualTo(500);
    assertThat(utilisation.get(0).volumeUtiliseM3()).isEqualTo(2.5);
    assertThat(utilisation.get(1).poidsUtiliseKg()).isZero();
  }

  @Test
  void obtenirVueCapaciteRetourneArretsEtTroncons() {
    preparerContexteVoyage(500, 2.5, arretDepart, arretMilieu, true);

    var vue = voyageCapaciteService.obtenirVueCapacite(voyageId);

    assertThat(vue.capaciteRemorque().poidsKg()).isEqualTo(1000);
    assertThat(vue.arrets()).hasSize(3);
    assertThat(vue.troncons()).hasSize(2);
    assertThat(vue.troncons().getFirst().poidsUtiliseKg()).isEqualTo(500);
  }

  @Test
  void verifierCapacitePourNouveauDossierAccepteUnDossierCompatible() {
    preparerContexteVoyage(500, 2.0, arretDepart, arretMilieu, true);

    ResultatVerification resultat =
        voyageCapaciteService.verifierCapacitePourNouveauDossier(
            voyageId, arretDepart, arretMilieu, 400, 3.0);

    assertThat(resultat.compatible()).isTrue();
  }

  @Test
  void verifierCapacitePourNouveauDossierRejetteUnDepassementPoids() {
    preparerContexteVoyage(900, 1.0, arretDepart, arretFin, true);

    ResultatVerification resultat =
        voyageCapaciteService.verifierCapacitePourNouveauDossier(
            voyageId, arretMilieu, arretFin, 200, 1.0);

    assertThat(resultat.compatible()).isFalse();
    assertThat(resultat.tronconsDepasses()).isNotEmpty();
  }

  private void preparerContexteVoyage(
      double poidsDossier, double volumeDossier, UUID arretChargement, UUID arretDechargement) {
    preparerContexteVoyage(poidsDossier, volumeDossier, arretChargement, arretDechargement, false);
  }

  private void preparerContexteVoyage(
      double poidsDossier,
      double volumeDossier,
      UUID arretChargement,
      UUID arretDechargement,
      boolean avecRemorque) {
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

    Voyage voyage =
        Voyage.creer(
            voyageId,
            Reference.generer("VOY", 2026, 1),
            TypeVoyage.SIMPLE,
            Portee.NATIONAL,
            depart,
            depart.plus(8, ChronoUnit.HOURS),
            UUID.randomUUID(),
            remorqueId,
            List.of(dossierId),
            trajet,
            List.of(new Affectation(UUID.randomUUID(), RoleChauffeur.TITULAIRE, depart)),
            0.5);

    when(voyageRepository.parId(voyageId)).thenReturn(Optional.of(voyage));
    when(voyageArretRepository.parVoyageIdOrdonnes(voyageId))
        .thenReturn(
            List.of(
                arret(arretDepart, voyageId, 0, "Paris"),
                arret(arretMilieu, voyageId, 1, "Lyon"),
                arret(arretFin, voyageId, 2, "Marseille")));
    when(dossierApi.listerPourCalculCapacite(List.of(dossierId)))
        .thenReturn(
            List.of(
                new DossierCapaciteSummary(
                    dossierId, poidsDossier, volumeDossier, arretChargement, arretDechargement)));
    if (avecRemorque) {
      when(remorqueApi.consulter(remorqueId))
          .thenReturn(
              Optional.of(
                  new RemorqueSummary(remorqueId, "REM-001", 20.0, 33, 1000, "DISPONIBLE")));
    }
  }

  private static ArretVoyage arret(UUID id, UUID voyageId, int indice, String libelle) {
    return ArretVoyage.creer(
        id, voyageId, indice, libelle, new GeoPoint(48.0 + indice, 2.0 + indice), null, true);
  }
}
