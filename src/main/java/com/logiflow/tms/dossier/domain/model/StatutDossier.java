package com.logiflow.tms.dossier.domain.model;

/** Cycle de vie d'un dossier de transport. */
public enum StatutDossier {
  CREE,
  PLANIFIE,
  EN_CHARGEMENT,
  CHARGE,
  EN_TRANSIT,
  EN_LIVRAISON,
  LIVRE,
  CLOTURE,
  INCIDENT,
  ANNULE
}
