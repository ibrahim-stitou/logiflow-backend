package com.logiflow.tms.planning.infrastructure.web.dto;

import com.logiflow.tms.planning.application.ConformiteVoyageService.AnomalieConformite;
import com.logiflow.tms.planning.application.ConformiteVoyageService.RapportConformite;
import java.util.Comparator;
import java.util.List;

/** Résultat du contrôle de conformité à blanc d'un projet de voyage. */
public record ConformiteVoyageResponse(
    boolean conforme,
    List<AnomalieConformite> anomalies,
    List<ArretVoyageResponse> arrets,
    double tauxRemplissage,
    double chargeMaxKg,
    double volumeMaxM3,
    int palettes,
    Double capaciteKg,
    Double capaciteM3,
    Integer capacitePalettes) {

  public static ConformiteVoyageResponse depuis(RapportConformite rapport) {
    return new ConformiteVoyageResponse(
        rapport.conforme(),
        rapport.anomalies(),
        rapport.arrets().stream()
            .sorted(Comparator.comparingInt(a -> a.indiceSequence()))
            .map(ArretVoyageResponse::depuis)
            .toList(),
        rapport.tauxRemplissage(),
        rapport.chargeMaxKg(),
        rapport.volumeMaxM3(),
        rapport.palettes(),
        rapport.capaciteKg(),
        rapport.capaciteM3(),
        rapport.capacitePalettes());
  }
}
