package com.logiflow.tms.planning.domain.model;

/** Cycle de vie d'un voyage. */
public enum StatutVoyage {
  BROUILLON,
  PLANIFIE,
  AFFECTE,
  EN_COURS,
  TERMINE,
  CLOTURE,
  ANNULE
}
