package com.logiflow.tms.ai.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Sous-ensemble de la réponse OSRM /route/v1/driving (GeoJSON). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OsrmRouteResponse(String code, List<Route> routes) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Route(Geometry geometry) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Geometry(String type, List<List<Double>> coordinates) {}
}
