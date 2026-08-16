package com.logiflow.tms.ai.infrastructure.client.dto;

import java.util.List;

/**
 * Corps de la réponse de Flask à POST /internal/ai/v1/itinerary/calculer. Voir
 * docs/integration-ia.md.
 */
public record ItineraryCalculerResponse(
    double distanceKm, double dureeMin, List<SegmentDto> segments) {

  public record PointDto(double latitude, double longitude, String libelle) {}

  public record SegmentDto(PointDto depart, PointDto arrivee, double distanceKm, double dureeMin) {}
}
