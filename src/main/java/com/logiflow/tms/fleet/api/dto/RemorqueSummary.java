package com.logiflow.tms.fleet.api.dto;

import java.util.UUID;

/** Vue publique et minimale d'une remorque, exposée aux autres modules (ex. {@code planning}). */
public record RemorqueSummary(
    UUID id,
    String immatriculation,
    double volumeUtileM3,
    int nbPositionsPalettes,
    double chargeUtileKg,
    String statut) {}
