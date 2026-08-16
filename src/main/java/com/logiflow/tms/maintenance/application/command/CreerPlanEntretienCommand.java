package com.logiflow.tms.maintenance.application.command;

import java.util.UUID;

public record CreerPlanEntretienCommand(
    UUID vehiculeId,
    String libelle,
    Integer periodiciteKm,
    Integer periodiciteMois,
    int seuilAlerteKm,
    int dureeEstimeeMin) {}
