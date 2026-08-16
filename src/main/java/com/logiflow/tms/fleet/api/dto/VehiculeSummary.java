package com.logiflow.tms.fleet.api.dto;

import java.util.UUID;

/** Vue publique et minimale d'un véhicule, exposée aux autres modules (planning, maintenance). */
public record VehiculeSummary(
    UUID id,
    String immatriculation,
    String type,
    double ptacKg,
    double chargeUtileKg,
    String statut) {}
