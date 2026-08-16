package com.logiflow.tms.referential.domain.service;

import com.logiflow.tms.referential.domain.model.Site;
import com.logiflow.tms.shared.domain.exception.ConflictException;
import java.util.Objects;

/** Règles métier du sous-domaine Site qui n'appartiennent à aucune entité en particulier. */
public class SiteDomainService {

  public void verifierCodeDisponible(String code, boolean codeDejaUtilise) {
    Objects.requireNonNull(code, "Le code du site est obligatoire");
    if (codeDejaUtilise) {
      throw new ConflictException("Un site avec le code '%s' existe déjà".formatted(code));
    }
  }

  /** Un site n'est accessible que si son gabarit d'accès autorise le poids du véhicule fourni. */
  public boolean estAccessiblePour(Site site, double poidsVehiculeKg) {
    Objects.requireNonNull(site, "Le site est obligatoire");
    if (poidsVehiculeKg < 0) {
      throw new IllegalArgumentException("Le poids du véhicule ne peut pas être négatif");
    }
    return site.estActif() && site.accessiblePour(poidsVehiculeKg);
  }
}
