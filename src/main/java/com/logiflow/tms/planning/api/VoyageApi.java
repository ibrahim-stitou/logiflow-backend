package com.logiflow.tms.planning.api;

import com.logiflow.tms.planning.api.dto.VoyageSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Voyage, seul point d'entrée autorisé pour les autres modules (ex.
 * {@code tracking} pour rattacher les événements d'exécution).
 */
public interface VoyageApi {

  /** Consulte la vue publique d'un voyage, vide s'il n'existe pas. */
  Optional<VoyageSummary> consulter(UUID voyageId);

  /**
   * Recherche paginée pour la consultation transverse (copilote IA). {@code statut} optionnel
   * ({@code null} = tous), doit appartenir à {@link #statutsConnus()} ; {@code texte} optionnel.
   */
  Page<VoyageSummary> rechercher(String texte, String statut, PageRequest pageRequest);

  /** Valeurs possibles du filtre de {@link #rechercher}. */
  List<String> statutsConnus();
}
