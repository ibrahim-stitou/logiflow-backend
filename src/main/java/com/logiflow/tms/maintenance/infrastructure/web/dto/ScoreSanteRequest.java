package com.logiflow.tms.maintenance.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.UUID;

public record ScoreSanteRequest(
    @NotNull UUID vehiculeId,
    @PositiveOrZero double score,
    int kmAvantEcheance,
    @NotNull LocalDate dateEcheanceProjetee,
    String recommandation) {}
