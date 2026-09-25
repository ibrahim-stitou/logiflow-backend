package com.logiflow.tms.fleet.api.dto;

import java.util.UUID;

/**
 * Vue d'un véhicule pour la planification de voyage : type, capacités (volume et palettes null si
 * non renseignés), carrosserie propre (porteur), froid, statut et validité des documents à la date
 * demandée.
 */
public record VehiculePlanificationSummary(
    UUID id,
    String immatriculation,
    String type,
    double chargeUtileKg,
    Double volumeUtileM3,
    Integer nbPositionsPalettes,
    String carrosserie,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax,
    String statut,
    boolean documentsValides) {}
