package com.logiflow.tms.iam.domain.service;

import com.logiflow.tms.shared.domain.exception.ConflictException;
import java.util.Objects;

/** Règles métier du module {@code iam} qui n'appartiennent à aucune entité en particulier. */
public class IamDomainService {

  public void verifierLoginDisponible(String login, boolean dejaUtilise) {
    Objects.requireNonNull(login, "Le login est obligatoire");
    if (dejaUtilise) {
      throw new ConflictException("Un utilisateur avec le login '%s' existe déjà".formatted(login));
    }
  }
}
