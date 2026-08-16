package com.logiflow.tms.referential.api.dto;

import java.util.UUID;

/**
 * Vue publique et minimale d'un site, exposée aux autres modules via {@link
 * com.logiflow.tms.referential.api.SiteApi}.
 */
public record SiteSummary(
    UUID id, String code, String libelle, double latitude, double longitude, boolean actif) {}
