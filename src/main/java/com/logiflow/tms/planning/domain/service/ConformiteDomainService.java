package com.logiflow.tms.planning.domain.service;

import com.logiflow.tms.shared.domain.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;

/**
 * Moteur de conformité bloquant : vérifie qu'une affectation de ressources respecte l'ensemble des
 * règles dures avant qu'un voyage ne puisse passer au statut AFFECTE.
 *
 * <p>Les signaux d'entrée (disponibilité, documents, habilitations, temps de conduite) sont
 * calculés par la couche application à partir des API publiques des modules {@code fleet} et {@code
 * driver} : le domaine ne dépend jamais directement d'un autre module (règle d'architecture 5).
 */
public class ConformiteDomainService {

  public void verifierConformite(CriteresConformite criteres) {
    List<String> violations = new ArrayList<>();
    if (!criteres.vehiculeDisponible()) {
      violations.add("Le véhicule n'est pas disponible sur la période demandée");
    }
    if (!criteres.documentsVehiculeValides()) {
      violations.add("Les documents du véhicule ne sont pas tous valides");
    }
    if (!criteres.chauffeurDisponible()) {
      violations.add("Le chauffeur n'est pas disponible sur la période demandée");
    }
    if (!criteres.habilitationAdrConforme()) {
      violations.add(
          "Le chauffeur ne possède pas l'habilitation ADR requise par la marchandise transportée");
    }
    if (!criteres.tempsConduiteSuffisant()) {
      violations.add("Le solde de temps de conduite du chauffeur est insuffisant pour ce voyage");
    }
    if (!violations.isEmpty()) {
      throw new ValidationException("Affectation non conforme", violations);
    }
  }

  /**
   * Signaux de conformité pré-calculés par la couche application via les API des autres modules.
   */
  public record CriteresConformite(
      boolean vehiculeDisponible,
      boolean documentsVehiculeValides,
      boolean chauffeurDisponible,
      boolean habilitationAdrConforme,
      boolean tempsConduiteSuffisant) {}
}
