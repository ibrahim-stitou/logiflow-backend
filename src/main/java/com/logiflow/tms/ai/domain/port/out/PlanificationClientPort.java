package com.logiflow.tms.ai.domain.port.out;

import com.logiflow.tms.ai.domain.model.planification.ContextePlanification;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification;

/** Port de sortie vers l'agent de planification de voyage du service IA externe. */
public interface PlanificationClientPort {

  /**
   * @throws com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException si le service IA
   *     ne peut pas être joint ou répond en erreur
   */
  ResultatPlanification proposer(ContextePlanification contexte);
}
