package com.logiflow.tms.ai.domain.model;

import java.util.Objects;

/** Tronçon d'itinéraire entre deux points de passage consécutifs. */
public record SegmentItineraire(
    PointItineraire depart, PointItineraire arrivee, double distanceKm, double dureeMin) {

  public SegmentItineraire {
    Objects.requireNonNull(depart, "Le point de départ du segment est obligatoire");
    Objects.requireNonNull(arrivee, "Le point d'arrivée du segment est obligatoire");
  }
}
