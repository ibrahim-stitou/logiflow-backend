package com.logiflow.tms.maintenance.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record PlanEntretienRequest(
    @NotNull UUID vehiculeId,
    @NotBlank String libelle,
    Integer periodiciteKm,
    Integer periodiciteMois,
    @PositiveOrZero int seuilAlerteKm,
    @PositiveOrZero int dureeEstimeeMin) {}
