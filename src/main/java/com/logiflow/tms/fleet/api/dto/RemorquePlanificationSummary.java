package com.logiflow.tms.fleet.api.dto;

import java.util.UUID;

/**
 * Vue d'une remorque pour la planification de voyage : carrosserie, capacités, froid, statut et
 * validité des documents à la date demandée.
 */
public record RemorquePlanificationSummary(
    UUID id,
    String immatriculation,
    String type,
    String carrosserie,
    double chargeUtileKg,
    double volumeUtileM3,
    int nbPositionsPalettes,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax,
    String statut,
    boolean documentsValides) {}
