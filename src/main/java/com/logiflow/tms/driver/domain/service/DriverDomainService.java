package com.logiflow.tms.driver.domain.service;

import com.logiflow.tms.shared.domain.exception.ConflictException;
import java.util.Objects;

/** Règles métier du module {@code driver} qui n'appartiennent à aucune entité en particulier. */
public class DriverDomainService {

  public void verifierMatriculeDisponible(String matricule, boolean dejaUtilise) {
    Objects.requireNonNull(matricule, "Le matricule est obligatoire");
    if (dejaUtilise) {
      throw new ConflictException(
          "Un chauffeur avec le matricule '%s' existe déjà".formatted(matricule));
    }
  }
}
