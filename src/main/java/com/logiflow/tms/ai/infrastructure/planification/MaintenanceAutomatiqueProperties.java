package com.logiflow.tms.ai.infrastructure.planification;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Déclenchements automatiques de l'agent de maintenance prédictive, préfixe {@code
 * logiflow.ai.maintenance.*}.
 *
 * @param analyseNocturne analyse planifiée de toute la flotte (expression {@code cron})
 * @param reanalyseSurEvenement réanalyse d'un engin après clôture d'OT, sinistre ou document
 * @param horizonJours horizon des échéances analysées
 */
@ConfigurationProperties(prefix = "logiflow.ai.maintenance")
@Validated
public record MaintenanceAutomatiqueProperties(
    boolean analyseNocturne,
    String cron,
    boolean reanalyseSurEvenement,
    @Min(1) @Max(180) int horizonJours) {}
