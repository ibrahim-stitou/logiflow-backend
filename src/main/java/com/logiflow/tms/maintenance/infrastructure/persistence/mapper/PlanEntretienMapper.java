package com.logiflow.tms.maintenance.infrastructure.persistence.mapper;

import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.PlanEntretienEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlanEntretienMapper {

  default PlanEntretien versDomaine(PlanEntretienEntity entity) {
    if (entity == null) {
      return null;
    }
    return PlanEntretien.reconstituer(
        entity.getId(),
        entity.getVehiculeId(),
        entity.getLibelle(),
        entity.getPeriodiciteKm(),
        entity.getPeriodiciteMois(),
        entity.getSeuilAlerteKm(),
        entity.getDureeEstimeeMin());
  }

  default PlanEntretienEntity versEntite(PlanEntretien plan) {
    if (plan == null) {
      return null;
    }
    return PlanEntretienEntity.builder()
        .id(plan.id())
        .vehiculeId(plan.vehiculeId())
        .libelle(plan.libelle())
        .periodiciteKm(plan.periodiciteKm())
        .periodiciteMois(plan.periodiciteMois())
        .seuilAlerteKm(plan.seuilAlerteKm())
        .dureeEstimeeMin(plan.dureeEstimeeMin())
        .build();
  }
}
