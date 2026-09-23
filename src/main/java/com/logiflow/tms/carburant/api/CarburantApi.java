package com.logiflow.tms.carburant.api;

import com.logiflow.tms.carburant.api.dto.ConsommationCarburantSummary;
import java.time.LocalDate;
import java.util.UUID;

/** Contrat public du module carburant pour les autres modules (ex. document). */
public interface CarburantApi {

  /** Lève une exception métier si la prise n'est pas en brouillon modifiable. */
  void verifierPriseModifiable(UUID priseId);

  /** Consommation agrégée entre deux dates incluses (fuseau Europe/Paris). */
  ConsommationCarburantSummary consommation(UUID vehiculeId, LocalDate debut, LocalDate fin);
}
