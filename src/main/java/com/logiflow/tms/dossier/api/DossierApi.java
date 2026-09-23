package com.logiflow.tms.dossier.api;

import com.logiflow.tms.dossier.api.dto.DossierCapaciteSummary;
import com.logiflow.tms.dossier.api.dto.DossierSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Dossier de transport, seul point d'entrée autorisé pour les autres
 * modules (ex. {@code planning} pour le groupage et l'affectation).
 */
public interface DossierApi {

  /** Consulte la vue publique d'un dossier, vide s'il n'existe pas. */
  Optional<DossierSummary> consulter(UUID dossierId);

  /**
   * Retourne les données de capacité des dossiers demandés (poids, volume, arrêts voyage). Les
   * identifiants inconnus sont ignorés.
   */
  List<DossierCapaciteSummary> listerPourCalculCapacite(List<UUID> dossierIds);

  /** Arrêts voyage encore référencés par les dossiers fournis (chargement ou déchargement). */
  Set<UUID> listerArretsVoyageReferences(List<UUID> dossierIds);

  /**
   * Marque les dossiers comme planifiés après création d'un voyage. Chaque dossier doit être au
   * statut {@code CREE}.
   */
  void planifierPourVoyage(List<UUID> dossierIds);

  /**
   * Repasse en {@code CREE} les dossiers encore {@code PLANIFIE} lorsqu'un voyage est annulé. Les
   * dossiers déjà en exécution ne sont pas modifiés.
   */
  void replanifierApresAnnulationVoyage(List<UUID> dossierIds);

  /**
   * Affecte les arrêts voyage de chargement/déchargement à un dossier déjà planifié (sans changer
   * le statut).
   */
  void affecterArretsVoyage(UUID dossierId, UUID arretChargementId, UUID arretDechargementId);

  /**
   * Planifie un dossier {@code CREE} sur un voyage en lui affectant ses arrêts de
   * chargement/déchargement.
   */
  void planifierSurVoyageAvecArrets(
      UUID dossierId, UUID arretChargementId, UUID arretDechargementId);

  /**
   * Recherche paginée pour la consultation transverse (copilote IA). {@code statut} optionnel
   * ({@code null} = tous), doit appartenir à {@link #statutsConnus()} ; {@code texte} optionnel.
   */
  Page<DossierSummary> rechercher(String texte, String statut, PageRequest pageRequest);

  /** Valeurs possibles du filtre de {@link #rechercher}. */
  List<String> statutsConnus();
}
