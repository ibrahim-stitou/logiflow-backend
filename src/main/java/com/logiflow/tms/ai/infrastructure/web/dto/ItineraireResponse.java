package com.logiflow.tms.ai.infrastructure.web.dto;

import com.logiflow.tms.ai.domain.model.ItineraireCalcule;
import com.logiflow.tms.ai.domain.model.PointItineraire;
import com.logiflow.tms.ai.domain.model.SegmentItineraire;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.List;

public record ItineraireResponse(
    double distanceKm,
    double dureeMin,
    List<SegmentResponse> segments,
    List<GeoPoint> geometrie) {

  public record PointResponse(double latitude, double longitude, String libelle) {
    static PointResponse depuis(PointItineraire point) {
      return new PointResponse(
          point.position().latitude(), point.position().longitude(), point.libelle());
    }
  }

  public record SegmentResponse(
      PointResponse depart, PointResponse arrivee, double distanceKm, double dureeMin) {
    static SegmentResponse depuis(SegmentItineraire segment) {
      return new SegmentResponse(
          PointResponse.depuis(segment.depart()),
          PointResponse.depuis(segment.arrivee()),
          segment.distanceKm(),
          segment.dureeMin());
    }
  }

  public static ItineraireResponse depuis(ItineraireCalcule itineraire) {
    return new ItineraireResponse(
        itineraire.distanceKm(),
        itineraire.dureeMin(),
        itineraire.segments().stream().map(SegmentResponse::depuis).toList(),
        itineraire.geometrie());
  }
}
