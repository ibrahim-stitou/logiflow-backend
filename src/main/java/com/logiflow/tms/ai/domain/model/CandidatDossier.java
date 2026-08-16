package com.logiflow.tms.ai.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Représentation minimale d'un dossier de transport candidat au groupage, telle que reçue depuis
 * {@code dossier.api.DossierApi}. Vocabulaire propre au module {@code ai} : il ne dépend jamais du
 * domaine du module {@code dossier} (règle d'architecture 5).
 */
public record CandidatDossier(
    UUID id,
    String reference,
    double poidsBrutKg,
    double volumeM3,
    int nbPalettes,
    boolean contientAdr,
    boolean groupable) {

  public CandidatDossier {
    Objects.requireNonNull(id, "L'identifiant du dossier est obligatoire");
    Objects.requireNonNull(reference, "La référence du dossier est obligatoire");
  }
}
