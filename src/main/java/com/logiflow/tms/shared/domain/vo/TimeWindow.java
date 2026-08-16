package com.logiflow.tms.shared.domain.vo;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Fenêtre temporelle demi-ouverte [début, fin[. */
public record TimeWindow(Instant debut, Instant fin) {

  public TimeWindow {
    Objects.requireNonNull(debut, "La date de début est obligatoire");
    Objects.requireNonNull(fin, "La date de fin est obligatoire");
    if (!fin.isAfter(debut)) {
      throw new IllegalArgumentException("La date de fin doit être postérieure à la date de début");
    }
  }

  public Duration duree() {
    return Duration.between(debut, fin);
  }

  public boolean chevauche(TimeWindow autre) {
    Objects.requireNonNull(autre, "La fenêtre à comparer est obligatoire");
    return debut.isBefore(autre.fin) && autre.debut.isBefore(fin);
  }

  public boolean contient(Instant instant) {
    Objects.requireNonNull(instant, "L'instant à vérifier est obligatoire");
    return !instant.isBefore(debut) && instant.isBefore(fin);
  }
}
