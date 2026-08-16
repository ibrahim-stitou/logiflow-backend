package com.logiflow.tms.referential.api.dto;

import java.util.UUID;

/**
 * Vue publique et minimale d'un client, exposée aux autres modules via {@link
 * com.logiflow.tms.referential.api.ClientApi}.
 */
public record ClientSummary(UUID id, String code, String raisonSociale, boolean actif) {}
