package com.logiflow.tms.maintenance.api.dto;

import java.time.Instant;
import java.util.UUID;

/** Engin retenu à l'atelier sur une période (OT planifié ou en cours avec immobilisation). */
public record IndisponibiliteSummary(
    String typeEngin, UUID enginId, String reference, String titre, Instant debut, Instant fin) {}
