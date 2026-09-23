package com.logiflow.tms.driver.domain.port.out;

import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des chauffeurs. */
public interface ChauffeurRepository {

  Chauffeur sauvegarder(Chauffeur chauffeur);

  Optional<Chauffeur> parId(UUID id);

  Optional<Chauffeur> parMatricule(String matricule);

  boolean existeParMatricule(String matricule);

  Page<Chauffeur> rechercher(String texteRecherche, PageRequest pageRequest);

  /**
   * Recherche filtrée par disponibilité opérationnelle (optionnelle, {@code null} = tous) et texte
   * (optionnel), triée de la plus récemment modifiée à la plus ancienne.
   */
  Page<Chauffeur> rechercherParDisponibilite(
      String texteRecherche, String disponibilite, PageRequest pageRequest);
}
