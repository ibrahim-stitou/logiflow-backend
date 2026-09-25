package com.logiflow.tms.dossier.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Point d'arrêt requis par un dossier, vu par le planificateur. {@code type} : {@code CHARGEMENT}
 * ou {@code DECHARGEMENT} ; la fenêtre horaire est demi-ouverte [debut, fin[.
 */
public record SegmentPlanificationSummary(
    String type, int ordre, UUID siteId, Instant debut, Instant fin) {}
