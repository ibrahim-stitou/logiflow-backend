package com.logiflow.tms.maintenance.api.dto;

import java.util.UUID;

/** Vue publique d'un plan d'entretien préventif. */
public record PlanEntretienSummary(
    UUID id,
    UUID vehiculeId,
    String libelle,
    Integer periodiciteKm,
    Integer periodiciteMois,
    int seuilAlerteKm) {}
