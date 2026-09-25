package com.logiflow.tms.maintenance.domain.model;

/** Nature technique d'une intervention de maintenance. */
public enum TypeIntervention {
  ENTRETIEN_PREVENTIF,
  REPARATION,
  CONTROLE_TECHNIQUE,
  PNEUMATIQUES,
  CARROSSERIE,
  DIAGNOSTIC,
  FREINAGE,
  GROUPE_FROID,
  RAPPEL_CONSTRUCTEUR,
  AUTRE
}
