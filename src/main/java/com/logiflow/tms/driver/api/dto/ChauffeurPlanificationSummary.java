package com.logiflow.tms.driver.api.dto;

import java.util.List;
import java.util.UUID;

/**
 * Vue d'un chauffeur pour la planification de voyage, évaluée à une date : catégories de permis,
 * habilitations valides, passeport, site de rattachement et motifs génériques de non-affectation
 * (statut, disponibilité, pièces expirées — hors exigences propres à un voyage).
 */
public record ChauffeurPlanificationSummary(
    UUID id,
    String matricule,
    String nom,
    String prenom,
    String statut,
    String disponibilite,
    long soldeTempsConduiteMinutes,
    List<String> categoriesPermis,
    List<String> habilitationsValides,
    boolean passeportValide,
    UUID siteRattachementId,
    List<String> motifsNonAffectation) {

  public ChauffeurPlanificationSummary {
    categoriesPermis = List.copyOf(categoriesPermis);
    habilitationsValides = List.copyOf(habilitationsValides);
    motifsNonAffectation = List.copyOf(motifsNonAffectation);
  }
}
