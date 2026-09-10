package com.logiflow.tms.dossier.api;

import com.logiflow.tms.dossier.api.dto.DossierSummary;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Dossier de transport, seul point d'entrée autorisé pour les autres
 * modules (ex. {@code planning} pour le groupage et l'affectation).
 */
public interface DossierApi {

  /** Consulte la vue publique d'un dossier, vide s'il n'existe pas. */
  Optional<DossierSummary> consulter(UUID dossierId);

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
}
