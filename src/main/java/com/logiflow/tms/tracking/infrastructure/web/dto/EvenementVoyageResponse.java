package com.logiflow.tms.tracking.infrastructure.web.dto;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.tracking.domain.model.EvenementVoyage;
import java.time.Instant;
import java.util.UUID;

public record EvenementVoyageResponse(
    UUID id,
    UUID voyageId,
    String type,
    Instant horodatage,
    GeoPoint position,
    String commentaire) {

  public static EvenementVoyageResponse depuis(EvenementVoyage evenement) {
    return new EvenementVoyageResponse(
        evenement.id(),
        evenement.voyageId(),
        evenement.type().name(),
        evenement.horodatage(),
        evenement.position(),
        evenement.commentaire());
  }
}
