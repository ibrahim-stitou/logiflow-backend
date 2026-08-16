package com.logiflow.tms.fleet.infrastructure.web.dto;

import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.fleet.domain.vo.DocumentVehicule;
import java.util.List;
import java.util.UUID;

public record VehiculeResponse(
    UUID id,
    String immatriculation,
    String type,
    double ptacKg,
    double chargeUtileKg,
    int kilometrage,
    int heuresMoteur,
    String statut,
    List<DocumentVehicule> documents) {

  public static VehiculeResponse depuis(Vehicule vehicule) {
    return new VehiculeResponse(
        vehicule.id(),
        vehicule.immatriculation().valeur(),
        vehicule.type().name(),
        vehicule.ptac().kg(),
        vehicule.chargeUtile().kg(),
        vehicule.kilometrage(),
        vehicule.heuresMoteur(),
        vehicule.statut().name(),
        vehicule.documents());
  }
}
