package com.logiflow.tms.ai.infrastructure.planification;

import com.logiflow.tms.ai.application.AnalyseMaintenanceAutomatique;
import com.logiflow.tms.document.api.DocumentEntiteModificationEvent;
import com.logiflow.tms.maintenance.api.EtatMaintenanceEnginModifieEvent;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Réanalyse un engin dès que son état de maintenance change, après validation de la transaction
 * (écoute asynchrone) : clôture d'un OT, sinistre déclaré, immobilisant ou clos, document du
 * véhicule ou de la remorque ajouté ou supprimé (contrôle technique, assurance…).
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "logiflow.ai.maintenance",
    name = "reanalyse-sur-evenement",
    havingValue = "true",
    matchIfMissing = true)
class ReanalyseMaintenanceListener {

  private static final Set<String> ENTITES_ENGIN = Set.of("VEHICULE", "REMORQUE");

  private final AnalyseMaintenanceAutomatique analyse;
  private final MaintenanceAutomatiqueProperties properties;

  @ApplicationModuleListener
  void surEtatMaintenanceModifie(EtatMaintenanceEnginModifieEvent event) {
    analyse.reanalyserEngin(event.enginId(), event.motif(), properties.horizonJours());
  }

  @ApplicationModuleListener
  void surDocumentModifie(DocumentEntiteModificationEvent event) {
    if (ENTITES_ENGIN.contains(event.typeEntite())) {
      analyse.reanalyserEngin(
          event.entiteId(), "document de l'engin modifié", properties.horizonJours());
    }
  }
}
