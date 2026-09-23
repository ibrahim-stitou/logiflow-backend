package com.logiflow.tms.carburant.api;

import java.util.UUID;

/** Contrat public du module carburant pour les autres modules (ex. document). */
public interface CarburantApi {

  /** Lève une exception métier si la prise n'est pas en brouillon modifiable. */
  void verifierPriseModifiable(UUID priseId);
}
