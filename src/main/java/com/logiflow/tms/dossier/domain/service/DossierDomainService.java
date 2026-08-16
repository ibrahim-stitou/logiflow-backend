package com.logiflow.tms.dossier.domain.service;

import com.logiflow.tms.dossier.domain.model.DossierTransport;
import java.util.List;
import java.util.Objects;

/** Règles métier du module {@code dossier} qui n'appartiennent à aucune entité en particulier. */
public class DossierDomainService {

  /**
   * Filtre grossier de préfiltrage du groupage : ne garde que les dossiers compatibles "règles
   * dures".
   */
  public List<DossierTransport> filtrerCompatibles(
      DossierTransport reference, List<DossierTransport> candidats) {
    Objects.requireNonNull(reference, "Le dossier de référence est obligatoire");
    Objects.requireNonNull(candidats, "La liste de candidats est obligatoire");
    return candidats.stream().filter(reference::estGroupableAvec).toList();
  }
}
