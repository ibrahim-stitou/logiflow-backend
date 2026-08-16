package com.logiflow.tms.maintenance.domain.service;

import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import java.util.List;
import java.util.Objects;

/**
 * Règles métier du module {@code maintenance} qui n'appartiennent à aucune entité en particulier.
 */
public class MaintenanceDomainService {

  /**
   * Parmi les plans d'un véhicule, ceux dont le seuil d'alerte est franchi pour le kilométrage
   * restant donné.
   */
  public List<PlanEntretien> plansEnAlerte(List<PlanEntretien> plans, int kmRestantAvantEcheance) {
    Objects.requireNonNull(plans, "La liste de plans est obligatoire");
    return plans.stream().filter(plan -> plan.alerteFranchie(kmRestantAvantEcheance)).toList();
  }
}
