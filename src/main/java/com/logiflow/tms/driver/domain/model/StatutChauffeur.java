package com.logiflow.tms.driver.domain.model;

/**
 * Statut administratif du chauffeur (situation d'emploi), distinct de sa disponibilité
 * opérationnelle ({@link DisponibiliteChauffeur}).
 */
public enum StatutChauffeur {
  ACTIF,
  INACTIF,
  SUSPENDU
}
