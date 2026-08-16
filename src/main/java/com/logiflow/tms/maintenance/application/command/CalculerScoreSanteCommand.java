package com.logiflow.tms.maintenance.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record CalculerScoreSanteCommand(
    UUID vehiculeId,
    double score,
    int kmAvantEcheance,
    LocalDate dateEcheanceProjetee,
    String recommandation) {}
