package com.logiflow.tms.ai.domain.model.planification;

import java.time.Instant;
import java.util.List;

/**
 * Réponse de l'agent de planification : options de voyage comparées, dont une recommandée. {@code
 * sourceDistances} vaut OSRM ou HAVERSINE, {@code sourceRedaction} LLM ou GABARIT.
 */
public record ResultatPlanification(
    List<Option> options,
    String comparaison,
    List<String> dossiersNonPlanifiables,
    String sourceDistances,
    String sourceRedaction) {

  public ResultatPlanification {
    options = options == null ? List.of() : List.copyOf(options);
    dossiersNonPlanifiables =
        dossiersNonPlanifiables == null ? List.of() : List.copyOf(dossiersNonPlanifiables);
  }

  public record Arret(
      int ordre,
      String siteId,
      String libelle,
      double latitude,
      double longitude,
      List<String> dossiersCharges,
      List<String> dossiersDecharges,
      Instant eta,
      Instant etd,
      double distanceDepuisPrecedentKm,
      double dureeDepuisPrecedentMin,
      double chargeApresKg,
      double attenteMin,
      boolean fenetreRespectee) {}

  public record Indicateurs(
      int nbDossiers,
      double distanceKm,
      int dureeConduiteMin,
      int dureeTotaleMin,
      double poidsKg,
      double volumeM3,
      int palettes,
      double tauxRemplissagePoids,
      double tauxRemplissageVolume,
      int fenetresManquees,
      double coutEstime,
      Double coutParTonne) {}

  public record Option(
      int rang,
      String objectif,
      String libelleObjectif,
      String typeVoyage,
      List<String> dossierIds,
      List<Arret> arrets,
      String vehiculeId,
      String remorqueId,
      List<String> chauffeurIds,
      Instant departPrevu,
      Instant arriveePrevue,
      Indicateurs indicateurs,
      List<String> alertes,
      String justification,
      boolean recommandee) {}
}
