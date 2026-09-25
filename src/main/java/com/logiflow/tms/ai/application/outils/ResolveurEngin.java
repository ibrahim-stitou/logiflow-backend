package com.logiflow.tms.ai.application.outils;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorqueSummary;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Retrouve un engin de maintenance (véhicule ou remorque) à partir d'une immatriculation saisie
 * librement, et libelle les engins cités dans les résultats des outils.
 */
@Component
class ResolveurEngin {

  /** Engin résolu : {@code type} vaut VEHICULE ou REMORQUE (type de source copilote). */
  record Engin(UUID id, String type, String immatriculation) {}

  private final ResolveurVehicule resolveurVehicule;
  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;

  ResolveurEngin(
      ResolveurVehicule resolveurVehicule, VehiculeApi vehiculeApi, RemorqueApi remorqueApi) {
    this.resolveurVehicule = resolveurVehicule;
    this.vehiculeApi = vehiculeApi;
    this.remorqueApi = remorqueApi;
  }

  /**
   * @throws ValidationException aucun véhicule ni aucune remorque ne correspond
   */
  Engin parImmatriculation(String saisie) {
    try {
      VehiculeSummary v = resolveurVehicule.parImmatriculation(saisie);
      return new Engin(v.id(), "VEHICULE", v.immatriculation());
    } catch (ValidationException vehiculeInconnu) {
      String cible = ResolveurVehicule.normaliser(saisie);
      for (String essai : ResolveurVehicule.variantes(saisie)) {
        for (RemorqueSummary r :
            remorqueApi.rechercher(essai, null, PageRequest.premiere(10)).contenu()) {
          if (ResolveurVehicule.normaliser(r.immatriculation()).equals(cible)) {
            return new Engin(r.id(), "REMORQUE", r.immatriculation());
          }
        }
      }
      throw new ValidationException(
          "Aucun véhicule ni aucune remorque avec l'immatriculation « %s ».".formatted(saisie),
          List.of("immatriculation: inconnue"));
    }
  }

  /** Index de libellés par identifiant, alimenté à la demande (véhicule puis remorque). */
  Libelles libelles() {
    return new Libelles();
  }

  final class Libelles {
    private final Map<UUID, Engin> cache = new HashMap<>();

    Engin engin(UUID id) {
      if (id == null) {
        return null;
      }
      return cache.computeIfAbsent(
          id,
          cle ->
              vehiculeApi
                  .consulter(cle)
                  .map(v -> new Engin(v.id(), "VEHICULE", v.immatriculation()))
                  .or(
                      () ->
                          remorqueApi
                              .consulter(cle)
                              .map(r -> new Engin(r.id(), "REMORQUE", r.immatriculation())))
                  .orElse(null));
    }

    String immatriculation(UUID id) {
      Engin e = engin(id);
      return e == null ? null : e.immatriculation();
    }

    /** Sources copilote des engins libellés jusqu'ici. */
    List<SourceCopilote> sources() {
      return cache.values().stream()
          .filter(Objects::nonNull)
          .map(e -> new SourceCopilote(e.type(), e.immatriculation(), e.id().toString()))
          .toList();
    }
  }
}
