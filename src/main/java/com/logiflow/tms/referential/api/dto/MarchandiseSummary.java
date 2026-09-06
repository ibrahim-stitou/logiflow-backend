package com.logiflow.tms.referential.api.dto;

import java.util.UUID;

/**
 * Vue publique et minimale d'une marchandise du catalogue, exposée aux autres modules (ex. {@code
 * order}, {@code dossier} pour valider et afficher les lignes de marchandise).
 */
public record MarchandiseSummary(
    UUID id,
    String code,
    String libelle,
    String famille,
    String classeAdr,
    String numeroOnu,
    boolean gerbable,
    boolean actif) {}
