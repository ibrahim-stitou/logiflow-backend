package com.logiflow.tms.carburant.infrastructure.web.dto;

import com.logiflow.tms.carburant.domain.model.Station;
import java.util.UUID;

public record StationResponse(UUID id, String code, String libelle, String adresse, boolean actif) {

  public static StationResponse depuis(Station station) {
    return new StationResponse(
        station.id(), station.code(), station.libelle(), station.adresse(), station.estActif());
  }
}
