package com.logiflow.tms.ai.domain.model;

import java.util.List;

/** Itinéraire routier calculé par l'agent itinéraire (service IA externe, moteur OSRM). */
public record ItineraireCalcule(
    double distanceKm, double dureeMin, List<SegmentItineraire> segments) {

  public ItineraireCalcule {
    segments = segments != null ? List.copyOf(segments) : List.of();
  }
}
