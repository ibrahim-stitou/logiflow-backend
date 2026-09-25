package com.logiflow.tms.planning.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Voyage non annulé sur une période, avec son véhicule et sa remorque éventuelle : sert à mesurer
 * l'usage des engins et à trouver les créneaux libres.
 */
public record ActiviteVoyageSummary(
    UUID vehiculeId,
    UUID remorqueId,
    String reference,
    String statut,
    Instant departPrevu,
    Instant arriveePrevue,
    double distanceKm) {}
