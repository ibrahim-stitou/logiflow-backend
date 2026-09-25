package com.logiflow.tms.planning.api;

import com.logiflow.tms.planning.api.dto.ActiviteVoyageSummary;
import com.logiflow.tms.planning.api.dto.ConformiteVoyageSummary;
import com.logiflow.tms.planning.api.dto.ProjetVoyageDto;
import com.logiflow.tms.planning.api.dto.RessourcesOccupeesSummary;
import com.logiflow.tms.planning.api.dto.VoyageSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.time.Instant;
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

  /** Véhicules, remorques et chauffeurs engagés sur un voyage actif pendant [debut, fin]. */
  RessourcesOccupeesSummary ressourcesOccupees(Instant debut, Instant fin);

  /**
   * Contrôle à blanc d'un projet de voyage avec les règles de la création (aucune écriture) : sert
   * à revalider les propositions de l'agent de planification.
   */
  ConformiteVoyageSummary evaluerConformite(ProjetVoyageDto projet);

  /** Voyages non annulés dont la période chevauche [debut, fin], tous véhicules confondus. */
  List<ActiviteVoyageSummary> activiteVehicules(Instant debut, Instant fin);
}
