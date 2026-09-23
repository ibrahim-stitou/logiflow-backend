package com.logiflow.tms.carburant.application.command;

import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreerPriseCarburantCommand(
    UUID voyageId,
    UUID vehiculeId,
    UUID remorqueId,
    UUID stationId,
    TypeCarburant typeCarburant,
    double litrage,
    BigDecimal montantTtc,
    Instant datePrise) {}
