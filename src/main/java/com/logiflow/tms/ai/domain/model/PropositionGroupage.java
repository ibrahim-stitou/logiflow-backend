package com.logiflow.tms.ai.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Proposition de regroupement de dossiers, produite par l'agent de groupage (IA) ou, en repli, par
 * le filtrage déterministe local lorsque le service IA est indisponible ({@code genereParIa =
 * false}).
 */
public record PropositionGroupage(
    List<UUID> dossierIds,
    Double score,
    Double confiance,
    Double gainKm,
    Double gainMarge,
    String justification,
    boolean genereParIa) {

  public PropositionGroupage {
    Objects.requireNonNull(dossierIds, "La liste de dossiers est obligatoire");
    dossierIds = List.copyOf(dossierIds);
    if (dossierIds.size() < 2) {
      throw new IllegalArgumentException(
          "Une proposition de groupage doit porter sur au moins deux dossiers");
    }
  }
}
