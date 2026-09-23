package com.logiflow.tms.planning.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.DossierSurTroncons;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.MotifDepassement;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.ResultatVerification;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.UtilisationTroncon;
import com.logiflow.tms.shared.domain.vo.Capacite;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CapaciteTronconDomainServiceTest {

  private CapaciteTronconDomainService service;

  private UUID arretA;
  private UUID arretB;
  private UUID arretC;
  private UUID arretD;
  private List<ArretVoyage> arretsQuatrePoints;

  @BeforeEach
  void setUp() {
    service = new CapaciteTronconDomainService();
    UUID voyageId = UUID.randomUUID();
    arretA = UUID.randomUUID();
    arretB = UUID.randomUUID();
    arretC = UUID.randomUUID();
    arretD = UUID.randomUUID();
    arretsQuatrePoints =
        List.of(
            arret(arretA, voyageId, 0, "Paris"),
            arret(arretB, voyageId, 1, "Lyon"),
            arret(arretC, voyageId, 2, "Marseille"),
            arret(arretD, voyageId, 3, "Nice"));
  }

  @Test
  void unDossierSurUnTronconIntermediaireNeChargeQueCeTroncon() {
    List<DossierSurTroncons> dossiers =
        List.of(new DossierSurTroncons(1000, 5.0, 1, 2));

    List<UtilisationTroncon> utilisation =
        service.calculerUtilisation(arretsQuatrePoints, dossiers);

    assertThat(utilisation).hasSize(3);
    assertThat(utilisation.get(0).poidsUtiliseKg()).isZero();
    assertThat(utilisation.get(1).poidsUtiliseKg()).isEqualTo(1000);
    assertThat(utilisation.get(1).volumeUtiliseM3()).isEqualTo(5.0);
    assertThat(utilisation.get(2).poidsUtiliseKg()).isZero();
  }

  @Test
  void unDossierSurToutLeVoyageChargeTousLesTroncons() {
    List<DossierSurTroncons> dossiers =
        List.of(new DossierSurTroncons(800, 4.0, 0, 3));

    List<UtilisationTroncon> utilisation =
        service.calculerUtilisation(arretsQuatrePoints, dossiers);

    assertThat(utilisation).allMatch(t -> t.poidsUtiliseKg() == 800);
    assertThat(utilisation).allMatch(t -> t.volumeUtiliseM3() == 4.0);
  }

  @Test
  void unNouveauDossierCompatibleEstAccepte() {
    List<DossierSurTroncons> dossiers =
        List.of(new DossierSurTroncons(500, 2.0, 0, 2));
    List<UtilisationTroncon> utilisation =
        service.calculerUtilisation(arretsQuatrePoints, dossiers);
    Capacite max = new Capacite(2000, 20.0, 33);

    ResultatVerification resultat =
        service.verifierAjoutDossier(utilisation, max, 1, 3, 400, 3.0);

    assertThat(resultat.compatible()).isTrue();
    assertThat(resultat.tronconsDepasses()).isEmpty();
  }

  @Test
  void unDepassementPoidsSurUnSeulTronconEstSignale() {
    List<DossierSurTroncons> dossiers =
        List.of(new DossierSurTroncons(900, 1.0, 0, 3));
    List<UtilisationTroncon> utilisation =
        service.calculerUtilisation(arretsQuatrePoints, dossiers);
    Capacite max = new Capacite(1000, 20.0, 33);

    ResultatVerification resultat =
        service.verifierAjoutDossier(utilisation, max, 1, 3, 200, 1.0);

    assertThat(resultat.compatible()).isFalse();
    assertThat(resultat.tronconsDepasses())
        .filteredOn(t -> t.motif() == MotifDepassement.POIDS)
        .hasSize(2)
        .first()
        .satisfies(
            t -> {
              assertThat(t.arretDepartId()).isEqualTo(arretB);
              assertThat(t.arretArriveeId()).isEqualTo(arretC);
              assertThat(t.depassementKg()).isEqualTo(100);
            });
  }

  @Test
  void uneRouteDeDeuxArretsExposeUnSeulTroncon() {
    UUID voyageId = UUID.randomUUID();
    List<ArretVoyage> deuxArrets =
        List.of(
            arret(arretA, voyageId, 0, "Départ"),
            arret(arretB, voyageId, 1, "Arrivée"));

    List<UtilisationTroncon> utilisation =
        service.calculerUtilisation(
            deuxArrets, List.of(new DossierSurTroncons(600, 2.0, 0, 1)));

    assertThat(utilisation).hasSize(1);
    assertThat(utilisation.getFirst().poidsUtiliseKg()).isEqualTo(600);
  }

  @Test
  void unDepassementVolumeEstSignale() {
    List<UtilisationTroncon> utilisation =
        service.calculerUtilisation(arretsQuatrePoints, List.of());
    Capacite max = new Capacite(5000, 10.0, 33);

    ResultatVerification resultat =
        service.verifierAjoutDossier(utilisation, max, 0, 3, 100, 12.0);

    assertThat(resultat.compatible()).isFalse();
    assertThat(resultat.tronconsDepasses())
        .allMatch(t -> t.motif() == MotifDepassement.VOLUME);
  }

  private static ArretVoyage arret(UUID id, UUID voyageId, int indice, String libelle) {
    return ArretVoyage.creer(
        id, voyageId, indice, libelle, new GeoPoint(48.0 + indice, 2.0 + indice), null, true);
  }
}
