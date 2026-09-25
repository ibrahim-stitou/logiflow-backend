package com.logiflow.tms.ai.infrastructure.web.dto;

import com.logiflow.tms.ai.application.PlanificationVoyageService.OptionValidee;
import com.logiflow.tms.ai.application.PlanificationVoyageService.PropositionsVoyage;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification.Arret;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification.Indicateurs;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification.Option;
import com.logiflow.tms.planning.api.dto.ConformiteVoyageSummary;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Propositions de voyages comparées. Chaque option porte un {@code voyage} au format de {@code POST
 * /api/v1/voyages} : l'écran de planification le reprend tel quel pour pré-remplir le formulaire
 * (relecture par l'exploitant avant création).
 */
public record PropositionsVoyageResponse(
    String portee,
    List<OptionResponse> options,
    String comparaison,
    List<String> dossiersNonPlanifiables,
    int nbDossiersCandidats,
    String sourceDistances,
    String sourceRedaction,
    Libelles libelles) {

  public record Libelles(
      Map<String, String> dossiers,
      Map<String, String> vehicules,
      Map<String, String> remorques,
      Map<String, String> chauffeurs) {}

  public record OptionResponse(
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
      boolean recommandee,
      ConformiteVoyageSummary conformite,
      VoyagePret voyage) {}

  public record Etape(
      int ordre,
      String type,
      Instant eta,
      Instant etd,
      double distanceDepuisPrecedenteKm,
      double chargeApresKg) {}

  public record Trajet(
      double distanceTotaleKm, int dureeConduiteMin, int dureeTotaleMin, List<Etape> etapes) {}

  public record Affectation(String chauffeurId, String role, Instant dateAffectation) {}

  public record ArretImpose(String siteId) {}

  public record VoyagePret(
      String typeVoyage,
      String portee,
      Instant departPrevu,
      Instant arriveePrevue,
      String vehiculeId,
      String remorqueId,
      List<String> dossierIds,
      Trajet trajet,
      List<Affectation> affectations,
      List<ArretImpose> arrets) {}

  public static PropositionsVoyageResponse depuis(PropositionsVoyage propositions) {
    return new PropositionsVoyageResponse(
        propositions.portee(),
        propositions.options().stream().map(o -> option(o, propositions.portee())).toList(),
        propositions.comparaison(),
        propositions.dossiersNonPlanifiables(),
        propositions.nbDossiersCandidats(),
        propositions.sourceDistances(),
        propositions.sourceRedaction(),
        new Libelles(
            propositions.libellesDossiers(),
            propositions.libellesVehicules(),
            propositions.libellesRemorques(),
            propositions.libellesChauffeurs()));
  }

  private static OptionResponse option(OptionValidee validee, String portee) {
    Option o = validee.option();
    return new OptionResponse(
        o.rang(),
        o.objectif(),
        o.libelleObjectif(),
        o.typeVoyage(),
        o.dossierIds(),
        o.arrets(),
        o.vehiculeId(),
        o.remorqueId(),
        o.chauffeurIds(),
        o.departPrevu(),
        o.arriveePrevue(),
        o.indicateurs(),
        o.alertes(),
        o.justification(),
        o.recommandee(),
        validee.conformite(),
        voyage(o, portee));
  }

  private static VoyagePret voyage(Option o, String portee) {
    List<Etape> etapes = new ArrayList<>();
    for (Arret a : o.arrets()) {
      boolean derniere = a.ordre() == o.arrets().size() - 1;
      etapes.add(
          new Etape(
              a.ordre(),
              a.dossiersCharges().isEmpty() ? "DECHARGEMENT" : "CHARGEMENT",
              a.eta(),
              derniere ? null : a.etd(),
              a.distanceDepuisPrecedentKm(),
              a.chargeApresKg()));
    }
    Indicateurs i = o.indicateurs();
    List<Affectation> affectations = new ArrayList<>();
    for (int k = 0; k < o.chauffeurIds().size(); k++) {
      affectations.add(
          new Affectation(
              o.chauffeurIds().get(k), k == 0 ? "TITULAIRE" : "RENFORT", o.departPrevu()));
    }
    return new VoyagePret(
        o.typeVoyage(),
        portee,
        o.departPrevu(),
        o.arriveePrevue(),
        o.vehiculeId(),
        o.remorqueId(),
        o.dossierIds(),
        new Trajet(
            i.distanceKm(),
            i.dureeConduiteMin(),
            Math.max(i.dureeConduiteMin(), i.dureeTotaleMin()),
            etapes),
        affectations,
        o.arrets().stream().map(a -> new ArretImpose(a.siteId())).toList());
  }
}
