package com.logiflow.tms.tracking.api.dto;

import java.time.Instant;
import java.util.UUID;

/** Vue publique et minimale du dernier événement connu d'un voyage. */
public record EvenementVoyageSummary(
    UUID voyageId, String type, Instant horodatage, Double latitude, Double longitude) {}
