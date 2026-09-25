package com.logiflow.tms.ai.application;

import com.logiflow.tms.ai.application.MaintenancePredictiveService.AnalyserMaintenanceCommand;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Déclenchements automatiques de l'agent de maintenance prédictive (analyse de nuit de la flotte,
 * réanalyse d'un engin après un événement métier). Les scores de santé sont enregistrés.
 *
 * <p>Ces exécutions ne sont jamais bloquantes : un service IA indisponible ou un engin retiré du
 * service est journalisé et la prochaine exécution reprendra. Une seule analyse de flotte à la fois
 * et une seule réanalyse par engin à la fois (les événements rapprochés se recouvrent).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyseMaintenanceAutomatique {

  private final MaintenancePredictiveService service;

  private final AtomicBoolean flotteEnCours = new AtomicBoolean();
  private final Set<UUID> enginsEnCours = ConcurrentHashMap.newKeySet();

  /** Analyse de toute la flotte ; vide si une analyse est déjà en cours ou a échoué. */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public Optional<ResultatMaintenance> analyserFlotte(int horizonJours) {
    if (!flotteEnCours.compareAndSet(false, true)) {
      log.info("Analyse de maintenance de la flotte déjà en cours : déclenchement ignoré");
      return Optional.empty();
    }
    try {
      ResultatMaintenance resultat =
          service.analyser(new AnalyserMaintenanceCommand(null, horizonJours, true));
      log.info(
          "Analyse de maintenance de la flotte : {} engin(s), scores enregistrés",
          resultat.vehicules().size());
      return Optional.of(resultat);
    } catch (RuntimeException e) {
      log.warn("Analyse de maintenance de la flotte en échec : {}", e.getMessage());
      return Optional.empty();
    } finally {
      flotteEnCours.set(false);
    }
  }

  /**
   * Réanalyse d'un engin après un événement ; sans effet si une réanalyse est déjà en cours. Hors
   * transaction englobante : un échec de l'analyse (transaction propre) ne compromet pas celle de
   * l'écouteur d'événement.
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void reanalyserEngin(UUID enginId, String motif, int horizonJours) {
    if (!enginsEnCours.add(enginId)) {
      return;
    }
    try {
      service.analyser(new AnalyserMaintenanceCommand(enginId, horizonJours, true));
      log.info("Score de santé recalculé pour l'engin {} ({})", enginId, motif);
    } catch (NotFoundException e) {
      log.debug("Engin {} hors service, pas de réanalyse ({})", enginId, motif);
    } catch (RuntimeException e) {
      log.warn("Réanalyse de maintenance de l'engin {} en échec : {}", enginId, e.getMessage());
    } finally {
      enginsEnCours.remove(enginId);
    }
  }
}
