package com.logiflow.tms.ai.domain.model;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;

/** Itinéraire routier calculé par l'agent itinéraire (service IA externe, moteur OSRM). */
public record ItineraireCalcule(
    double distanceKm,
    double dureeMin,
    List<SegmentItineraire> segments,
    List<GeoPoint> geometrie) {

  public ItineraireCalcule {
    segments = segments != null ? List.copyOf(segments) : List.of();
    geometrie = geometrie != null ? List.copyOf(geometrie) : List.of();
  }

  public ItineraireCalcule(
      double distanceKm, double dureeMin, List<SegmentItineraire> segments) {
    this(distanceKm, dureeMin, segments, List.of());
  }
}
