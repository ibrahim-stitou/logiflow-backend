package com.logiflow.tms.driver.domain.model;

/** Disponibilité opérationnelle du chauffeur, consultée pour l'affectation aux voyages. */
public enum DisponibiliteChauffeur {
  DISPONIBLE,
  EN_VOYAGE,
  EN_REPOS,
  EN_CONGE,
  INDISPONIBLE
}
