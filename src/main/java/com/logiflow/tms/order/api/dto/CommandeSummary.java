package com.logiflow.tms.order.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Vue publique et minimale d'une commande, exposée aux autres modules (ex. {@code dossier}). */
public record CommandeSummary(
    UUID id, String reference, UUID clientId, String statut, LocalDate dateSouhaitee) {}
