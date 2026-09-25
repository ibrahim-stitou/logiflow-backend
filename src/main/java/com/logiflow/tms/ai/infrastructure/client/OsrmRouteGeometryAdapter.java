package com.logiflow.tms.ai.infrastructure.client;

import com.logiflow.tms.ai.domain.model.PointItineraire;
import com.logiflow.tms.ai.domain.port.out.RouteGeometryPort;
import com.logiflow.tms.ai.infrastructure.client.dto.OsrmRouteResponse;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class OsrmRouteGeometryAdapter implements RouteGeometryPort {

  /**
   * Écart max. (km) entre la fin OSRM et la destination de l'étape avant relance routière /
   * raccord.
   */
  private static final double ECART_DESTINATION_KM = 5.0;

  private final RestClient osrmRestClient;

  @Override
  public List<GeoPoint> resoudreGeometrie(List<PointItineraire> points) {
    if (points.size() < 2) {
      return List.of();
    }

    List<GeoPoint> geometrie = new ArrayList<>();
    for (int i = 0; i < points.size() - 1; i++) {
      GeoPoint depart = points.get(i).position();
      GeoPoint arrivee = points.get(i + 1).position();
      List<GeoPoint> etape = resoudreEtape(depart, arrivee);
      fusionnerEtape(geometrie, etape);
    }

    return geometrie.size() >= 2 ? geometrie : List.of();
  }

  private List<GeoPoint> resoudreEtape(GeoPoint depart, GeoPoint arrivee) {
    List<GeoPoint> routee;
    try {
      routee = appelerOsrm(List.of(depart, arrivee));
    } catch (ServiceIndisponibleException e) {
      return segmentDroit(depart, arrivee);
    }

    if (routee.size() < 2) {
      return segmentDroit(depart, arrivee);
    }

    // OSRM (ex. extract Europe) peut snapper loin du vrai départ (Marrakech → Tarifa).
    List<GeoPoint> resultat = new ArrayList<>();
    GeoPoint debutRoutee = routee.getFirst();
    if (debutRoutee.distanceHaversineKm(depart) > ECART_DESTINATION_KM) {
      resultat.addAll(raccorderVers(debutRoutee, depart, true));
    }
    fusionnerEtape(resultat, routee);

    GeoPoint finRoutee = resultat.get(resultat.size() - 1);
    if (finRoutee.distanceHaversineKm(arrivee) > ECART_DESTINATION_KM) {
      fusionnerEtape(resultat, raccorderVers(finRoutee, arrivee, false));
    }

    return resultat.size() >= 2 ? resultat : segmentDroit(depart, arrivee);
  }

  /**
   * Relie un point hors graphe OSRM à un point routable : tente d'abord une route OSRM, sinon ligne
   * droite (traversée maritime / continent hors couverture).
   *
   * @param depuisPointAncre point déjà sur la géométrie OSRM
   * @param depuisOuVersDepart si true, ancre = début OSRM et cible = vrai départ (préfixe)
   */
  private List<GeoPoint> raccorderVers(
      GeoPoint pointAncre, GeoPoint cible, boolean prefixeVersDepart) {
    GeoPoint a = prefixeVersDepart ? cible : pointAncre;
    GeoPoint b = prefixeVersDepart ? pointAncre : cible;
    try {
      List<GeoPoint> routee = appelerOsrm(List.of(a, b));
      if (routee.size() >= 2
          && routee.getFirst().distanceHaversineKm(a) <= ECART_DESTINATION_KM
          && routee.get(routee.size() - 1).distanceHaversineKm(b) <= ECART_DESTINATION_KM) {
        return routee;
      }
    } catch (ServiceIndisponibleException ignored) {
      // Hors couverture OSRM (Afrique) ou traversée maritime.
    }
    return segmentDroit(a, b);
  }

  private List<GeoPoint> appelerOsrm(List<GeoPoint> waypoints) {
    if (waypoints.size() < 2) {
      return List.of();
    }

    String coordinatePath =
        waypoints.stream()
            .map(point -> point.longitude() + "," + point.latitude())
            .reduce((left, right) -> left + ";" + right)
            .orElse("");

    try {
      OsrmRouteResponse reponse =
          osrmRestClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/route/v1/driving/{coordinates}")
                          .queryParam("overview", "full")
                          .queryParam("geometries", "geojson")
                          .queryParam("continue_straight", "false")
                          .build(coordinatePath))
              .retrieve()
              .body(OsrmRouteResponse.class);

      if (reponse == null || !"Ok".equals(reponse.code())) {
        throw new ServiceIndisponibleException(
            "Le moteur de routage OSRM n'a pas renvoyé de trajet valide");
      }
      if (reponse.routes() == null || reponse.routes().isEmpty()) {
        throw new ServiceIndisponibleException(
            "Le moteur de routage OSRM n'a pas renvoyé de trajet valide");
      }

      OsrmRouteResponse.Geometry geometry = reponse.routes().getFirst().geometry();
      if (geometry == null || geometry.coordinates() == null || geometry.coordinates().isEmpty()) {
        throw new ServiceIndisponibleException(
            "Le moteur de routage OSRM n'a pas renvoyé de géométrie");
      }

      List<GeoPoint> geometrie = new ArrayList<>(geometry.coordinates().size());
      for (List<Double> coordinate : geometry.coordinates()) {
        if (coordinate == null || coordinate.size() < 2) {
          continue;
        }
        geometrie.add(new GeoPoint(coordinate.get(1), coordinate.get(0)));
      }

      if (geometrie.size() < 2) {
        throw new ServiceIndisponibleException(
            "Le moteur de routage OSRM n'a pas renvoyé de géométrie exploitable");
      }

      return geometrie;
    } catch (RestClientException e) {
      throw new ServiceIndisponibleException(
          "Le moteur de routage OSRM est momentanément indisponible", e);
    }
  }

  private static void fusionnerEtape(List<GeoPoint> accum, List<GeoPoint> etape) {
    if (etape.isEmpty()) {
      return;
    }
    if (accum.isEmpty()) {
      accum.addAll(etape);
      return;
    }
    GeoPoint dernier = accum.get(accum.size() - 1);
    int debut = 0;
    if (pointsProches(dernier, etape.getFirst())) {
      debut = 1;
    }
    if (debut < etape.size()) {
      accum.addAll(etape.subList(debut, etape.size()));
    }
  }

  private static boolean pointsProches(GeoPoint a, GeoPoint b) {
    return a.distanceHaversineKm(b) < 0.05;
  }

  /** Interpolation linéaire (traversée maritime ou raccord quand OSRM ne relie pas deux rives). */
  private static List<GeoPoint> segmentDroit(GeoPoint depart, GeoPoint arrivee) {
    double distanceKm = depart.distanceHaversineKm(arrivee);
    if (distanceKm < 0.05) {
      return List.of(depart, arrivee);
    }
    int pas = Math.max(2, (int) Math.ceil(distanceKm / 15.0));
    List<GeoPoint> points = new ArrayList<>(pas + 1);
    for (int i = 0; i <= pas; i++) {
      double t = (double) i / pas;
      points.add(
          new GeoPoint(
              depart.latitude() + t * (arrivee.latitude() - depart.latitude()),
              depart.longitude() + t * (arrivee.longitude() - depart.longitude())));
    }
    return points;
  }
}
