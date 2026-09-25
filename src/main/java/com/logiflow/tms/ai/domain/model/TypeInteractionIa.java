package com.logiflow.tms.ai.domain.model;

/**
 * Nature d'une interaction avec le service IA externe, à des fins de journalisation et
 * d'évaluation.
 */
public enum TypeInteractionIa {
  COPILOTE,
  /** Ancien agent de groupage, conservé pour l'historique du journal. */
  GROUPAGE,
  PLANIFICATION,
  MAINTENANCE,
  ITINERAIRE
}
