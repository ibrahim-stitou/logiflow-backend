package com.logiflow.tms.fleet.infrastructure.web.dto;

import com.logiflow.tms.fleet.domain.model.Remorque;
import java.util.UUID;

public record RemorqueResponse(
    UUID id,
    String immatriculation,
    String carrosserie,
    double volumeUtileM3,
    int nbPositionsPalettes,
    double chargeUtileKg,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax,
    String statut) {

  public static RemorqueResponse depuis(Remorque remorque) {
    var capacite = remorque.capaciteUtile();
    return new RemorqueResponse(
        remorque.id(),
        remorque.immatriculation().valeur(),
        remorque.carrosserie().name(),
        capacite.volumeM3(),
        capacite.positionsPalettes(),
        capacite.poidsKg(),
        remorque.groupeFroid(),
        remorque.temperatureMin(),
        remorque.temperatureMax(),
        remorque.statut().name());
  }
}
