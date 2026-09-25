package com.logiflow.tms.ai.infrastructure.planification;

import com.logiflow.tms.ai.application.AnalyseMaintenanceAutomatique;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Analyse de maintenance de toute la flotte chaque nuit (5 h, heure de Paris, par défaut) : le
 * tableau de bord et les fiches sont à jour à l'arrivée des exploitants.
 *
 * <p>Instance unique supposée : en déploiement multi-instances, ajouter un verrou partagé
 * (ShedLock) pour n'exécuter l'analyse qu'une fois.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "logiflow.ai.maintenance",
    name = "analyse-nocturne",
    havingValue = "true",
    matchIfMissing = true)
class AnalyseMaintenanceNocturne {

  private final AnalyseMaintenanceAutomatique analyse;
  private final MaintenanceAutomatiqueProperties properties;

  @Scheduled(cron = "${logiflow.ai.maintenance.cron:0 0 5 * * *}", zone = "Europe/Paris")
  void executer() {
    analyse.analyserFlotte(properties.horizonJours());
  }
}
