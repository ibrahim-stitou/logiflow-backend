package com.logiflow.tms.ai.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Requête de calcul d'itinéraire routier passant par une liste de points ordonnée. */
public record ItineraireRequest(@Valid @Size(min = 2) List<PointRequest> points) {

  public record PointRequest(
      @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
      @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
      String libelle) {}
}
