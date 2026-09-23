package com.logiflow.tms.carburant.infrastructure.web.dto;

import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import com.logiflow.tms.carburant.domain.port.out.PriseCarburantRepository;
import java.math.BigDecimal;
import java.util.List;

public record PriseCarburantStatsResponse(
    long nombre, double litresTotal, BigDecimal montantTotal, List<ParType> parType) {

  public record ParType(
      TypeCarburant type, long nombre, double litres, BigDecimal montant) {}

  public static PriseCarburantStatsResponse depuis(
      PriseCarburantRepository.PriseCarburantStats stats) {
    return new PriseCarburantStatsResponse(
        stats.nombre(),
        stats.litresTotal(),
        stats.montantTotal(),
        stats.parType().stream()
            .map(
                row ->
                    new ParType(row.type(), row.nombre(), row.litres(), row.montant()))
            .toList());
  }
}
