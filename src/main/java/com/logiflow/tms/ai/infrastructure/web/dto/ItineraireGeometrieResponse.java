package com.logiflow.tms.ai.infrastructure.web.dto;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;

/** Polyligne routière (OSRM) pour affichage cartographique. */
public record ItineraireGeometrieResponse(List<GeoPoint> geometrie) {

  public static ItineraireGeometrieResponse depuis(List<GeoPoint> geometrie) {
    return new ItineraireGeometrieResponse(List.copyOf(geometrie));
  }
}
