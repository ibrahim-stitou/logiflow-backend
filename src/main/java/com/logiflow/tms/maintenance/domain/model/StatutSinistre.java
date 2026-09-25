package com.logiflow.tms.maintenance.domain.model;

/** Cycle de vie d'un sinistre, de la déclaration à la clôture. */
public enum StatutSinistre {
  DECLARE,
  DECLARE_ASSUREUR,
  EN_EXPERTISE,
  EN_REPARATION,
  CLOS,
  CLASSE_SANS_SUITE
}
