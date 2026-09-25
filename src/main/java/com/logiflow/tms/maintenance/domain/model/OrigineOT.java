package com.logiflow.tms.maintenance.domain.model;

/** Ce qui a déclenché l'ordre de travail. */
public enum OrigineOT {
  MANUELLE,
  PLAN_ENTRETIEN,
  SINISTRE,
  AGENT_IA,
  PANNE_SIGNALEE
}
