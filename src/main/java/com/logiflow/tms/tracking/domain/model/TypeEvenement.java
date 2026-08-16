package com.logiflow.tms.tracking.domain.model;

/** Nature d'un événement d'exécution d'un voyage. */
public enum TypeEvenement {
  DEPART,
  ARRIVEE_CHARGEMENT,
  CHARGEMENT_TERMINE,
  ARRIVEE_DECHARGEMENT,
  LIVRAISON_TERMINEE,
  POSITION,
  INCIDENT,
  CLOTURE
}
