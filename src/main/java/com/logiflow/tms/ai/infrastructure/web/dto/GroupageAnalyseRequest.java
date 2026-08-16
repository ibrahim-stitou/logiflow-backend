package com.logiflow.tms.ai.infrastructure.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/**
 * Requête d'analyse de groupage sur un ensemble de dossiers candidats sélectionnés par
 * l'exploitant.
 */
public record GroupageAnalyseRequest(@NotEmpty List<UUID> dossierIds) {}
