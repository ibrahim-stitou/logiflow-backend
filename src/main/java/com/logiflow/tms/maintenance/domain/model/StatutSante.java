package com.logiflow.tms.maintenance.domain.model;

/** Niveau de santé d'un véhicule, dérivé de son score. */
public enum StatutSante {
  BON,
  SURVEILLER,
  A_PLANIFIER,
  CRITIQUE
}
