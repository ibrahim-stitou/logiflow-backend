package com.logiflow.tms.dossier.api.dto;

import java.util.UUID;

/**
 * Vue publique et minimale d'un dossier de transport, exposée aux autres modules (ex. {@code
 * planning}).
 */
public record DossierSummary(
    UUID id,
    String reference,
    String statut,
    boolean groupable,
    double poidsBrutKg,
    double volumeM3,
    int nbPalettes,
    boolean contientAdr) {}
