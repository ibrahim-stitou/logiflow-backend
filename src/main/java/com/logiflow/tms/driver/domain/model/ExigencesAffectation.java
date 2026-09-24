package com.logiflow.tms.driver.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Exigences d'un voyage envers ses chauffeurs, évaluées par {@link
 * Chauffeur#motifsNonAffectation(ExigencesAffectation)}.
 *
 * @param date date du départ du voyage (validité des habilitations et pièces)
 * @param adr le voyage transporte des marchandises dangereuses
 * @param international le voyage franchit une frontière (passeport et visa)
 * @param permisRequis catégorie minimale requise par le véhicule (null = non vérifiée)
 */
public record ExigencesAffectation(
    LocalDate date, boolean adr, boolean international, CategoriePermis permisRequis) {

  public ExigencesAffectation {
    Objects.requireNonNull(date, "La date du voyage est obligatoire");
  }
}
