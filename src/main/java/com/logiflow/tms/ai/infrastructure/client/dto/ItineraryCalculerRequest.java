package com.logiflow.tms.ai.infrastructure.client.dto;

import java.util.List;

/**
 * Corps de la requête POST /internal/ai/v1/itinerary/calculer envoyée à Flask. Voir
 * docs/integration-ia.md.
 */
public record ItineraryCalculerRequest(List<PointDto> points, String correlationId) {

  public record PointDto(double latitude, double longitude, String libelle) {}
}
