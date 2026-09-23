package com.logiflow.tms.carburant.infrastructure.persistence.mapper;

import com.logiflow.tms.carburant.domain.model.PriseCarburant;
import com.logiflow.tms.carburant.domain.model.StatutPrise;
import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import com.logiflow.tms.carburant.infrastructure.persistence.entity.PriseCarburantEntity;
import org.springframework.stereotype.Component;

@Component
public class PriseCarburantMapper {

  public PriseCarburant versDomaine(PriseCarburantEntity entity) {
    return PriseCarburant.reconstituer(
        entity.getId(),
        entity.getVoyageId(),
        entity.getVehiculeId(),
        entity.getRemorqueId(),
        entity.getStationId(),
        TypeCarburant.valueOf(entity.getTypeCarburant()),
        entity.getLitrage(),
        entity.getMontantTtc(),
        entity.getDatePrise(),
        StatutPrise.valueOf(entity.getStatut()));
  }

  public PriseCarburantEntity versEntite(PriseCarburant prise) {
    return PriseCarburantEntity.builder()
        .id(prise.id())
        .voyageId(prise.voyageId())
        .vehiculeId(prise.vehiculeId())
        .remorqueId(prise.remorqueId())
        .stationId(prise.stationId())
        .typeCarburant(prise.typeCarburant().name())
        .litrage(prise.litrage())
        .montantTtc(prise.montantTtc())
        .datePrise(prise.datePrise())
        .statut(prise.statut().name())
        .build();
  }

  public void mettreAJour(PriseCarburantEntity entity, PriseCarburant prise) {
    entity.ecraserEtatMetier(
        prise.stationId(),
        prise.typeCarburant().name(),
        prise.litrage(),
        prise.montantTtc(),
        prise.statut().name());
  }
}
