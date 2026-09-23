package com.logiflow.tms.planning.api.dto;

import java.util.List;
import java.util.UUID;

/** Vue publique et minimale d'un voyage, exposée aux autres modules (ex. {@code tracking}). */
public record VoyageSummary(
    UUID id,
    String reference,
    String statut,
    UUID vehiculeId,
    UUID remorqueId,
    List<UUID> dossierIds) {}
