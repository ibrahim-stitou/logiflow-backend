package com.logiflow.tms.tracking.domain.service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/** Règles métier du module {@code tracking} qui n'appartiennent à aucune entité en particulier. */
public class TrackingDomainService {

  /**
   * Un événement ne peut pas être antérieur au dernier événement déjà enregistré pour le voyage.
   */
  public void verifierChronologie(Instant nouvelHorodatage, Optional<Instant> dernierHorodatage) {
    Objects.requireNonNull(nouvelHorodatage, "L'horodatage est obligatoire");
    Objects.requireNonNull(dernierHorodatage, "Le dernier horodatage est obligatoire");
    if (dernierHorodatage.isPresent() && nouvelHorodatage.isBefore(dernierHorodatage.get())) {
      throw new IllegalArgumentException(
          "Un événement ne peut pas être antérieur au dernier événement déjà enregistré pour ce voyage");
    }
  }
}
