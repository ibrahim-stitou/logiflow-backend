package com.logiflow.tms.maintenance.infrastructure.web.dto;

import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.shared.domain.vo.Money;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrdreTravailResponse(
    UUID id,
    UUID vehiculeId,
    String type,
    String statut,
    LocalDateTime datePlanifiee,
    int dureeReelleMin,
    Money cout) {

  public static OrdreTravailResponse depuis(OrdreTravail ordreTravail) {
    return new OrdreTravailResponse(
        ordreTravail.id(),
        ordreTravail.vehiculeId(),
        ordreTravail.type().name(),
        ordreTravail.statut().name(),
        ordreTravail.datePlanifiee(),
        ordreTravail.dureeReelleMin(),
        ordreTravail.cout());
  }
}
