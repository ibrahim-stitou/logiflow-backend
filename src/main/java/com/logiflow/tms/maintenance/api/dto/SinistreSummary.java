package com.logiflow.tms.maintenance.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Vue publique d'un sinistre et de son coût net (réparations terminées − indemnité). */
public record SinistreSummary(
    UUID id,
    String reference,
    UUID vehiculeId,
    UUID remorqueId,
    UUID chauffeurId,
    LocalDateTime dateSurvenance,
    String type,
    String gravite,
    String responsabilite,
    String statut,
    boolean enginImmobilise,
    BigDecimal coutNet) {}
