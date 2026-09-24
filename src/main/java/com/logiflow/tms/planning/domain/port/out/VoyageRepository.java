package com.logiflow.tms.planning.domain.port.out;

import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des voyages. */
public interface VoyageRepository {

  Voyage sauvegarder(Voyage voyage);

  Optional<Voyage> parId(UUID id);

  /** Charge le voyage avec verrou pessimiste pour sérialiser les ajouts concurrents de dossiers. */
  Optional<Voyage> parIdAvecVerrouillage(UUID id);

  Optional<Voyage> parReference(String reference);

  Page<Voyage> rechercher(String texteRecherche, PageRequest pageRequest);

  /**
   * Recherche filtrée par statut (optionnel, {@code null} = tous) et texte (optionnel), triée de la
   * plus récemment modifiée à la plus ancienne.
   */
  Page<Voyage> rechercherParStatut(String texteRecherche, String statut, PageRequest pageRequest);

  List<Voyage> parDossierId(UUID dossierId);

  /** Voyages où le chauffeur est affecté (titulaire ou renfort), du plus récent au plus ancien. */
  List<Voyage> parChauffeurId(UUID chauffeurId);
}
