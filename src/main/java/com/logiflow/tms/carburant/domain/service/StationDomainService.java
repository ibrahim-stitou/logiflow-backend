package com.logiflow.tms.carburant.domain.service;

import com.logiflow.tms.shared.domain.exception.ConflictException;
import java.util.Objects;

/** Règles métier du sous-domaine Station. */
public class StationDomainService {

  public void verifierCodeDisponible(String code, boolean codeDejaUtilise) {
    Objects.requireNonNull(code, "Le code de la station est obligatoire");
    if (codeDejaUtilise) {
      throw new ConflictException("Une station avec le code '%s' existe déjà".formatted(code));
    }
  }
}
