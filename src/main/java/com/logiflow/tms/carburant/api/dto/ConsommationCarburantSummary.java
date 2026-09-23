package com.logiflow.tms.carburant.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Consommation de carburant agrégée sur une période, pour un véhicule ou toute la flotte ({@code
 * vehiculeId} null). Bornes incluses.
 */
public record ConsommationCarburantSummary(
    UUID vehiculeId,
    LocalDate debut,
    LocalDate fin,
    long nombrePrises,
    double litresTotal,
    BigDecimal montantTotalTtc,
    List<ParType> parType) {

  public record ParType(
      String typeCarburant, long nombrePrises, double litres, BigDecimal montantTtc) {}
}
