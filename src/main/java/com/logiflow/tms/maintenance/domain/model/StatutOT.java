package com.logiflow.tms.maintenance.domain.model;

/** Cycle de vie d'un ordre de travail. */
public enum StatutOT {
  PLANIFIE,
  EN_COURS,
  EN_ATTENTE_PIECES,
  TERMINE,
  ANNULE
}
