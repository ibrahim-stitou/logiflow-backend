package com.logiflow.tms.maintenance.infrastructure.web.dto;

import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import java.util.UUID;

public record PlanEntretienResponse(
    UUID id,
    UUID vehiculeId,
    String libelle,
    Integer periodiciteKm,
    Integer periodiciteMois,
    int seuilAlerteKm,
    int dureeEstimeeMin) {

  public static PlanEntretienResponse depuis(PlanEntretien plan) {
    return new PlanEntretienResponse(
        plan.id(),
        plan.vehiculeId(),
        plan.libelle(),
        plan.periodiciteKm(),
        plan.periodiciteMois(),
        plan.seuilAlerteKm(),
        plan.dureeEstimeeMin());
  }
}
