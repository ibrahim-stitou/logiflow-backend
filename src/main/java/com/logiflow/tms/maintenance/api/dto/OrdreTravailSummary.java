package com.logiflow.tms.maintenance.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Vue publique d'un ordre de travail ({@code typeEngin} : VEHICULE ou REMORQUE). */
public record OrdreTravailSummary(
    UUID id,
    String reference,
    String typeEngin,
    UUID enginId,
    String type,
    String nature,
    String statut,
    String titre,
    LocalDateTime debutPlanifie,
    LocalDateTime finPlanifiee,
    LocalDateTime finReelle,
    Integer kilometrage,
    boolean immobilisation,
    BigDecimal totalTtc) {}
