package com.logiflow.tms.maintenance.infrastructure.persistence.mapper;

import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.OrdreTravailEntity;
import com.logiflow.tms.shared.domain.vo.Money;
import java.util.Currency;
import java.util.UUID;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrdreTravailMapper {

  UUID TENANT_PAR_DEFAUT = UUID.fromString("00000000-0000-0000-0000-000000000000");

  default OrdreTravail versDomaine(OrdreTravailEntity entity) {
    if (entity == null) {
      return null;
    }
    return OrdreTravail.reconstituer(
        entity.getId(),
        entity.getVehiculeId(),
        TypeIntervention.valueOf(entity.getTypeIntervention()),
        StatutOT.valueOf(entity.getStatut()),
        entity.getDatePlanifiee(),
        entity.getDureeReelleMin(),
        new Money(entity.getCoutMontant(), Currency.getInstance(entity.getCoutDevise())));
  }

  default OrdreTravailEntity versEntite(OrdreTravail ordreTravail) {
    if (ordreTravail == null) {
      return null;
    }
    return OrdreTravailEntity.builder()
        .id(ordreTravail.id())
        .tenantId(TENANT_PAR_DEFAUT)
        .vehiculeId(ordreTravail.vehiculeId())
        .typeIntervention(ordreTravail.type().name())
        .statut(ordreTravail.statut().name())
        .datePlanifiee(ordreTravail.datePlanifiee())
        .dureeReelleMin(ordreTravail.dureeReelleMin())
        .coutMontant(ordreTravail.cout().montant())
        .coutDevise(ordreTravail.cout().devise().getCurrencyCode())
        .build();
  }
}
