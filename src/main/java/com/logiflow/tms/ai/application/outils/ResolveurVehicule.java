package com.logiflow.tms.ai.application.outils;

import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Retrouve un véhicule à partir d'une immatriculation saisie librement par l'utilisateur (« AB123CD
 * », « ab-123-cd »…) : le LLM ne connaît pas les identifiants techniques.
 */
@Component
class ResolveurVehicule {

  private final VehiculeApi vehiculeApi;

  ResolveurVehicule(VehiculeApi vehiculeApi) {
    this.vehiculeApi = vehiculeApi;
  }

  /**
   * @throws ValidationException aucun véhicule ne correspond (message renvoyé au LLM)
   */
  VehiculeSummary parImmatriculation(String saisie) {
    String cible = normaliser(saisie);
    for (String essai : variantes(saisie)) {
      List<VehiculeSummary> candidats =
          vehiculeApi.rechercher(essai, null, PageRequest.premiere(10)).contenu();
      for (VehiculeSummary candidat : candidats) {
        if (normaliser(candidat.immatriculation()).equals(cible)) {
          return candidat;
        }
      }
    }
    throw new ValidationException(
        "Aucun véhicule avec l'immatriculation « %s ».".formatted(saisie),
        List.of("immatriculation: inconnue"));
  }

  static String normaliser(String immatriculation) {
    return immatriculation.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
  }

  private static Set<String> variantes(String saisie) {
    Set<String> variantes = new LinkedHashSet<>();
    variantes.add(saisie.strip());
    String brut = normaliser(saisie);
    variantes.add(brut);
    if (brut.matches("[A-Z]{2}\\d{3}[A-Z]{2}")) {
      // Format SIV français : AB-123-CD.
      variantes.add(brut.substring(0, 2) + "-" + brut.substring(2, 5) + "-" + brut.substring(5));
    }
    return variantes;
  }
}
