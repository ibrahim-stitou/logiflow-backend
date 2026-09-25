package com.logiflow.tms.maintenance.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Vue publique d'un ordre de travail de maintenance. */
public record OrdreTravailSummary(
    UUID id,
    UUID vehiculeId,
    String type,
    String statut,
    LocalDateTime datePlanifiee,
    BigDecimal cout,
    String devise) {}
