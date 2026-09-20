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

  private final RestClient osrmRestClient;

  @Override
  public List<GeoPoint> resoudreGeometrie(List<PointItineraire> points) {
    if (points.size() < 2) {
      return List.of();
    }

    String coordinatePath =
        points.stream()
            .map(
                point ->
                    point.position().longitude() + "," + point.position().latitude())
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
}
