package com.logiflow.tms.carburant.infrastructure.persistence.mapper;

import com.logiflow.tms.carburant.domain.model.Station;
import com.logiflow.tms.carburant.infrastructure.persistence.entity.StationEntity;
import org.springframework.stereotype.Component;

@Component
public class StationMapper {

  public Station versDomaine(StationEntity entity) {
    return Station.reconstituer(
        entity.getId(),
        entity.getCode(),
        entity.getLibelle(),
        entity.getAdresse(),
        entity.isActif());
  }

  public StationEntity versEntite(Station station) {
    return StationEntity.builder()
        .id(station.id())
        .code(station.code())
        .libelle(station.libelle())
        .adresse(station.adresse())
        .actif(station.estActif())
        .build();
  }

  public void mettreAJour(StationEntity entity, Station station) {
    entity.ecraserEtatMetier(station.libelle(), station.adresse(), station.estActif());
  }
}
