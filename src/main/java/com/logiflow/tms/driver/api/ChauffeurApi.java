package com.logiflow.tms.driver.api;

import com.logiflow.tms.driver.api.dto.ChauffeurSummary;
import com.logiflow.tms.driver.api.dto.ExigencesAffectationDto;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Chauffeur, seul point d'entrée autorisé pour les autres modules
 * (ex. {@code planning} pour l'affectation).
 */
public interface ChauffeurApi {

  /** Consulte la vue publique d'un chauffeur, vide s'il n'existe pas. */
  Optional<ChauffeurSummary> consulter(UUID chauffeurId);

  /** Indique si le chauffeur existe et est au statut DISPONIBLE. */
  boolean estDisponible(UUID chauffeurId);

  /** Indique si le chauffeur possède une habilitation ADR de base valide à la date donnée. */
  boolean possedeHabilitationAdr(UUID chauffeurId, LocalDate date);

  /**
   * Indique si tous les documents obligatoires du chauffeur (module {@code document}) sont valides
   * à la date donnée.
   */
  boolean documentsValides(UUID chauffeurId, LocalDate date);

  /**
   * Recherche paginée pour la consultation transverse (copilote IA). {@code disponibilite}
   * optionnel ({@code null} = tous), doit appartenir à {@link #disponibilitesConnues()} ; {@code
   * texte} optionnel.
   */
  Page<ChauffeurSummary> rechercher(String texte, String disponibilite, PageRequest pageRequest);

  /** Valeurs possibles du filtre de {@link #rechercher}. */
  List<String> disponibilitesConnues();

  /**
   * Raisons pour lesquelles le chauffeur ne peut pas être affecté à un voyage ayant ces exigences :
   * statut, disponibilité, permis, habilitations (dont ADR), passeport/visa. Liste vide =
   * affectable ; chauffeur inconnu = un motif.
   */
  List<String> motifsNonAffectation(UUID chauffeurId, ExigencesAffectationDto exigences);
}
