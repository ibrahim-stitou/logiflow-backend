package com.logiflow.tms.fleet.domain.port.out;

import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des véhicules. */
public interface VehiculeRepository {

  Vehicule sauvegarder(Vehicule vehicule);

  Optional<Vehicule> parId(UUID id);

  Optional<Vehicule> parImmatriculation(String immatriculation);

  boolean existeParImmatriculation(String immatriculation);

  Page<Vehicule> rechercher(String texteRecherche, PageRequest pageRequest);

  /**
   * Recherche filtrée par statut (optionnel, {@code null} = tous) et texte (optionnel), triée de la
   * plus récemment modifiée à la plus ancienne.
   */
  Page<Vehicule> rechercherParStatut(String texteRecherche, String statut, PageRequest pageRequest);
}
