package com.logiflow.tms.ai.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Vérifie le raccordement de segments (fusion + ligne droite) pour les itinéraires multi-étapes.
 */
class RouteGeometrySegmentsTest {

  @Test
  void segmentDroitRelieDeuxPointsEloignes() throws Exception {
    Method segmentDroit =
        OsrmRouteGeometryAdapter.class.getDeclaredMethod(
            "segmentDroit", GeoPoint.class, GeoPoint.class);
    segmentDroit.setAccessible(true);

    GeoPoint casa = new GeoPoint(33.5731, -7.5898);
    GeoPoint barcelona = new GeoPoint(41.3851, 2.1734);
    @SuppressWarnings("unchecked")
    List<GeoPoint> ligne = (List<GeoPoint>) segmentDroit.invoke(null, casa, barcelona);

    assertThat(ligne).hasSizeGreaterThan(2);
    assertThat(ligne.getFirst().latitude()).isEqualTo(casa.latitude());
    assertThat(ligne.get(ligne.size() - 1).latitude()).isEqualTo(barcelona.latitude());
  }

  @Test
  void fusionnerEtapeEviteLesDoublons() throws Exception {
    Method fusionner =
        OsrmRouteGeometryAdapter.class.getDeclaredMethod("fusionnerEtape", List.class, List.class);
    fusionner.setAccessible(true);

    List<GeoPoint> accum =
        new ArrayList<>(List.of(new GeoPoint(33.0, -7.0), new GeoPoint(35.0, -5.5)));
    List<GeoPoint> etape =
        List.of(new GeoPoint(35.0, -5.5), new GeoPoint(36.0, -5.0), new GeoPoint(41.0, 2.0));
    fusionner.invoke(null, accum, etape);

    assertThat(accum).hasSize(4);
    assertThat(accum.get(1).latitude()).isEqualTo(35.0);
    assertThat(accum.get(accum.size() - 1).latitude()).isEqualTo(41.0);
  }

  @Test
  void segmentDroitPrefixeRelieMarrakechATarifa() throws Exception {
    Method segmentDroit =
        OsrmRouteGeometryAdapter.class.getDeclaredMethod(
            "segmentDroit", GeoPoint.class, GeoPoint.class);
    segmentDroit.setAccessible(true);

    GeoPoint marrakech = new GeoPoint(31.6295, -8.0089);
    GeoPoint tarifa = new GeoPoint(36.005278, -5.609366);
    @SuppressWarnings("unchecked")
    List<GeoPoint> ligne = (List<GeoPoint>) segmentDroit.invoke(null, marrakech, tarifa);

    assertThat(ligne.getFirst().latitude()).isEqualTo(marrakech.latitude());
    assertThat(ligne.get(ligne.size() - 1).latitude()).isEqualTo(tarifa.latitude());
    assertThat(ligne.size()).isGreaterThan(10);
  }
}
