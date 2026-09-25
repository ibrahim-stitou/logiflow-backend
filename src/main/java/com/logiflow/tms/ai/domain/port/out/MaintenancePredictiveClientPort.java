package com.logiflow.tms.ai.domain.port.out;

import com.logiflow.tms.ai.domain.model.maintenance.ContexteMaintenance;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;

/** Port de sortie vers l'agent de maintenance prédictive du service IA externe. */
public interface MaintenancePredictiveClientPort {

  /**
   * @throws com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException si le service IA
   *     ne peut pas être joint ou répond en erreur
   */
  ResultatMaintenance recommander(ContexteMaintenance contexte);
}
