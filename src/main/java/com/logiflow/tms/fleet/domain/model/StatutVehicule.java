package com.logiflow.tms.fleet.domain.model;

/** Cycle de vie d'un véhicule ou d'une remorque (statut partagé, mêmes états de disponibilité). */
public enum StatutVehicule {
  DISPONIBLE,
  RESERVE,
  EN_VOYAGE,
  EN_MAINTENANCE,
  IMMOBILISE,
  HORS_SERVICE
}
