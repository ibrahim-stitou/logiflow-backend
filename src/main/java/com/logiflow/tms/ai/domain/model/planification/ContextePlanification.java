package com.logiflow.tms.ai.domain.model.planification;

import java.time.Instant;
import java.util.List;

/**
 * Contexte transmis à l'agent de planification : dossiers {@code CREE} de la période, sites utiles
 * (dossiers et bases des chauffeurs) et ressources libres et conformes sur la période. Les
 * identifiants sont des chaînes (UUID) : l'agent ne fait aucun appel au backend.
 */
public record ContextePlanification(
    Instant debut,
    Instant fin,
    String typeVoyage,
    int nbOptions,
    List<Dossier> dossiers,
    List<Site> sites,
    List<Vehicule> vehicules,
    List<Remorque> remorques,
    List<Chauffeur> chauffeurs,
    String correlationId) {

  /** Copie portant l'identifiant de corrélation de la requête en cours (posé par l'adaptateur). */
  public ContextePlanification avecCorrelationId(String identifiant) {
    return new ContextePlanification(
        debut,
        fin,
        typeVoyage,
        nbOptions,
        dossiers,
        sites,
        vehicules,
        remorques,
        chauffeurs,
        identifiant);
  }

  public record FenetreSite(String siteId, Instant debut, Instant fin) {}

  public record Dossier(
      String id,
      String reference,
      double poidsBrutKg,
      double volumeM3,
      int nbPalettes,
      boolean contientAdr,
      boolean groupable,
      boolean international,
      String carrosserieRequise,
      Double temperatureRequise,
      FenetreSite chargement,
      FenetreSite dechargement) {}

  public record Site(String id, String libelle, double latitude, double longitude) {}

  public record Vehicule(
      String id,
      String immatriculation,
      String type,
      double chargeUtileKg,
      Double volumeUtileM3,
      Integer nbPositionsPalettes,
      String carrosserie,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax) {}

  public record Remorque(
      String id,
      String immatriculation,
      String carrosserie,
      double chargeUtileKg,
      double volumeUtileM3,
      int nbPositionsPalettes,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax) {}

  public record Chauffeur(
      String id,
      String matricule,
      String nom,
      String prenom,
      long soldeTempsConduiteMinutes,
      List<String> categoriesPermis,
      List<String> habilitations,
      boolean passeportValide,
      String siteRattachementId) {}
}
