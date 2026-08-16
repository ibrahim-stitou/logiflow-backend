package com.logiflow.tms.maintenance.infrastructure.web.dto;

import com.logiflow.tms.maintenance.domain.model.ScoreSante;
import java.time.LocalDate;
import java.util.UUID;

public record ScoreSanteResponse(
    UUID id,
    UUID vehiculeId,
    LocalDate calculeLe,
    double score,
    String statut,
    int kmAvantEcheance,
    LocalDate dateEcheanceProjetee,
    String recommandation,
    boolean necessiteIntervention) {

  public static ScoreSanteResponse depuis(ScoreSante scoreSante) {
    return new ScoreSanteResponse(
        scoreSante.id(),
        scoreSante.vehiculeId(),
        scoreSante.calculeLe(),
        scoreSante.score(),
        scoreSante.statut().name(),
        scoreSante.kmAvantEcheance(),
        scoreSante.dateEcheanceProjetee(),
        scoreSante.recommandation(),
        scoreSante.necessiteIntervention());
  }
}
