package com.logiflow.tms.carburant.infrastructure.web.dto;

import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record MajPriseCarburantRequest(
    @NotNull UUID stationId,
    @NotNull TypeCarburant typeCarburant,
    @Positive double litrage,
    @NotNull @Positive BigDecimal montantTtc) {}
