package com.logiflow.tms.dossier.domain.model;

/** Nature d'un point d'arrêt requis par un dossier de transport. */
public enum TypeSegment {
  CHARGEMENT,
  /** Arrêt intermédiaire (ex. hub ferry Tanger Med / Algeciras) sans chargement ni livaison. */
  ESCALE,
  DECHARGEMENT
}
