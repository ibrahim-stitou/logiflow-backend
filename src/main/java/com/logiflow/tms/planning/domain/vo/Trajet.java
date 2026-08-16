package com.logiflow.tms.planning.domain.vo;

import java.util.List;
import java.util.Objects;

/** Séquence ordonnée d'étapes d'un voyage, avec distances et durées estimées. */
public record Trajet(
    double distanceTotaleKm, int dureeConduiteMin, int dureeTotaleMin, List<Etape> etapes) {

  public Trajet {
    if (distanceTotaleKm < 0) {
      throw new IllegalArgumentException("La distance totale ne peut pas être négative");
    }
    if (dureeConduiteMin < 0) {
      throw new IllegalArgumentException("La durée de conduite ne peut pas être négative");
    }
    if (dureeTotaleMin < dureeConduiteMin) {
      throw new IllegalArgumentException(
          "La durée totale ne peut pas être inférieure à la durée de conduite");
    }
    Objects.requireNonNull(etapes, "La liste d'étapes est obligatoire");
    etapes = List.copyOf(etapes);
    if (etapes.size() < 2) {
      throw new IllegalArgumentException("Un trajet doit comporter au moins deux étapes");
    }
  }
}
