package com.logiflow.tms.planning.api.dto;

import java.time.Instant;
import java.util.UUID;

/** Voyage non annulé d'un véhicule sur une période : sert à mesurer l'usage et les créneaux. */
public record ActiviteVoyageSummary(
    UUID vehiculeId,
    String reference,
    String statut,
    Instant departPrevu,
    Instant arriveePrevue,
    double distanceKm) {}
