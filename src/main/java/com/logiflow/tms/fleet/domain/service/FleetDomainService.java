package com.logiflow.tms.fleet.domain.service;

import com.logiflow.tms.shared.domain.exception.ConflictException;
import java.util.Objects;

/** Règles métier du module {@code fleet} qui n'appartiennent à aucune entité en particulier. */
public class FleetDomainService {

  public void verifierImmatriculationDisponible(String immatriculation, boolean dejaUtilisee) {
    Objects.requireNonNull(immatriculation, "L'immatriculation est obligatoire");
    if (dejaUtilisee) {
      throw new ConflictException(
          "Un véhicule ou une remorque avec l'immatriculation '%s' existe déjà"
              .formatted(immatriculation));
    }
  }
}
