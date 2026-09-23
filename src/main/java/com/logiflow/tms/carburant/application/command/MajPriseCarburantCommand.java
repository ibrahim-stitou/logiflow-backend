package com.logiflow.tms.carburant.application.command;

import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import java.math.BigDecimal;
import java.util.UUID;

public record MajPriseCarburantCommand(
    UUID stationId, TypeCarburant typeCarburant, double litrage, BigDecimal montantTtc) {}
