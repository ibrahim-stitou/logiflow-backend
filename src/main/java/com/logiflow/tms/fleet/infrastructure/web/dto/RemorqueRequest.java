package com.logiflow.tms.fleet.infrastructure.web.dto;

import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Requête de création d'une remorque. */
public record RemorqueRequest(
    @NotBlank String immatriculation,
    @NotNull TypeCarrosserie carrosserie,
    @PositiveOrZero double volumeUtileM3,
    @PositiveOrZero int nbPositionsPalettes,
    @PositiveOrZero double chargeUtileKg,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax) {}
