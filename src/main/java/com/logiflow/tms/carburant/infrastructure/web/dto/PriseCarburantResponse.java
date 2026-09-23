package com.logiflow.tms.carburant.infrastructure.web.dto;

import com.logiflow.tms.carburant.domain.model.PriseCarburant;
import com.logiflow.tms.carburant.domain.model.StatutPrise;
import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

public record PriseCarburantResponse(
    UUID id,
    UUID voyageId,
    String voyageReference,
    UUID vehiculeId,
    UUID remorqueId,
    UUID stationId,
    String stationCode,
    String stationLibelle,
    TypeCarburant typeCarburant,
    double litrage,
    BigDecimal montantTtc,
    Double prixUnitaire,
    Instant datePrise,
    StatutPrise statut) {

  public static PriseCarburantResponse depuis(
      PriseCarburant prise, String voyageReference, String stationCode, String stationLibelle) {
    Double prixUnitaire = null;
    if (prise.litrage() > 0) {
      prixUnitaire =
          prise
              .montantTtc()
              .divide(BigDecimal.valueOf(prise.litrage()), 4, RoundingMode.HALF_UP)
              .doubleValue();
    }
    return new PriseCarburantResponse(
        prise.id(),
        prise.voyageId(),
        voyageReference,
        prise.vehiculeId(),
        prise.remorqueId(),
        prise.stationId(),
        stationCode,
        stationLibelle,
        prise.typeCarburant(),
        prise.litrage(),
        prise.montantTtc(),
        prixUnitaire,
        prise.datePrise(),
        prise.statut());
  }
}
