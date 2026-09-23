package com.logiflow.tms.dossier.api.dto;

import java.util.UUID;

/**
 * Vue publique d'un dossier pour le calcul de capacité par tronçon, exposée au module {@code
 * planning}.
 */
public record DossierCapaciteSummary(
    UUID id,
    double poidsBrutKg,
    double volumeM3,
    UUID arretChargementId,
    UUID arretDechargementId) {}
