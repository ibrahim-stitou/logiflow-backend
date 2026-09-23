package com.logiflow.tms.planning.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.service.InsertionItineraireDomainService.ResultatInsertion;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InsertionItineraireDomainServiceTest {

  private InsertionItineraireDomainService service;
  private UUID voyageId;
  private List<ArretVoyage> routeEquatoriale;

  @BeforeEach
  void setUp() {
    service = new InsertionItineraireDomainService();
    voyageId = UUID.randomUUID();
    routeEquatoriale =
        List.of(
            arret(0, "A", 0, 0),
            arret(1, "B", 0, 1),
            arret(2, "C", 0, 2));
  }

  @Test
  void uneRouteMinimaleDeDeuxArretsAccepteUneInsertion() {
    List<ArretVoyage> deuxArrets =
        List.of(arret(0, "Départ", 0, 0), arret(1, "Arrivée", 0, 2));

    ResultatInsertion resultat =
        service.trouverMeilleureInsertion(deuxArrets, new GeoPoint(0, 1));

    assertThat(resultat.apresIndiceArret()).isZero();
    assertThat(service.detourAcceptable(resultat.detourPourcent(), 25)).isTrue();
  }

  @Test
  void unPointSurLeTronconADetourQuasiNul() {
    ResultatInsertion resultat =
        service.trouverMeilleureInsertion(routeEquatoriale, new GeoPoint(0, 0.5));

    assertThat(resultat.apresIndiceArret()).isZero();
    assertThat(resultat.detourKm()).isLessThan(0.01);
    assertThat(resultat.detourPourcent()).isLessThan(0.01);
  }

  @Test
  void leDetourLePlusFaibleEstSelectionne() {
    GeoPoint pointProcheDuSecondTroncon = new GeoPoint(0, 1.5);

    ResultatInsertion resultat =
        service.trouverMeilleureInsertion(routeEquatoriale, pointProcheDuSecondTroncon);

    assertThat(resultat.apresIndiceArret()).isEqualTo(1);
  }

  @Test
  void unPointEloigneDepasseLeSeuilDeDeviation() {
    ResultatInsertion resultat =
        service.trouverMeilleureInsertion(routeEquatoriale, new GeoPoint(0.5, 0.5));

    assertThat(service.detourAcceptable(resultat.detourPourcent(), 25)).isFalse();
  }

  @Test
  void unPointLegerementDecaleResteSousUnSeuilEleve() {
    ResultatInsertion resultat =
        service.trouverMeilleureInsertion(routeEquatoriale, new GeoPoint(0.02, 0.5));

    assertThat(service.detourAcceptable(resultat.detourPourcent(), 25)).isTrue();
  }

  @Test
  void lInsertionRespecteLIndiceArretMinimum() {
    GeoPoint pointProcheDuPremierTroncon = new GeoPoint(0, 0.5);

    ResultatInsertion resultat =
        service.trouverMeilleureInsertion(routeEquatoriale, pointProcheDuPremierTroncon, 1);

    assertThat(resultat.apresIndiceArret()).isEqualTo(1);
  }

  @Test
  void uneRouteAvecUnSeulArretEstRejetee() {
    assertThatThrownBy(
            () ->
                service.trouverMeilleureInsertion(
                    List.of(arret(0, "A", 0, 0)), new GeoPoint(0, 0.5)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private ArretVoyage arret(int indice, String libelle, double lat, double lon) {
    return ArretVoyage.creer(
        UUID.randomUUID(), voyageId, indice, libelle, new GeoPoint(lat, lon), null, true);
  }
}
