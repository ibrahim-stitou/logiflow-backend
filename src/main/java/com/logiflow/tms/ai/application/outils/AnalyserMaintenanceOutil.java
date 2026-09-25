package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.application.MaintenancePredictiveService;
import com.logiflow.tms.ai.application.MaintenancePredictiveService.AnalyserMaintenanceCommand;
import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance.AnalyseVehicule;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Outil copilote de l'agent de maintenance prédictive. En lecture seule : les scores de santé ne
 * sont enregistrés que depuis l'écran Maintenance.
 */
@Component
class AnalyserMaintenanceOutil implements OutilCopilote {

  private static final int VEHICULES_MAX = 10;

  private final MaintenancePredictiveService service;
  private final ResolveurVehicule resolveurVehicule;

  AnalyserMaintenanceOutil(
      MaintenancePredictiveService service, ResolveurVehicule resolveurVehicule) {
    this.service = service;
    this.resolveurVehicule = resolveurVehicule;
  }

  @Override
  public String nom() {
    return "analyser_maintenance_predictive";
  }

  @Override
  public String libelle() {
    return "Analyse prédictive de la maintenance";
  }

  @Override
  public String description() {
    return "Analyse prédictive de la maintenance d'un véhicule (par immatriculation) ou de toute "
        + "la flotte sur les 30 prochains jours : score de santé, échéances d'entretien projetées "
        + "à partir de l'usage réel et des voyages planifiés, documents à renouveler, pannes "
        + "récurrentes, surconsommation, actions recommandées et créneau libre proposé. Pour la "
        + "flotte, renvoie les véhicules les plus à risque d'abord.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte(
            "immatriculation", "Immatriculation du véhicule (absent = toute la flotte)"));
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    String immatriculation = arguments.texte("immatriculation");
    VehiculeSummary vehicule =
        immatriculation == null ? null : resolveurVehicule.parImmatriculation(immatriculation);
    ResultatMaintenance resultat =
        service.analyser(
            new AnalyserMaintenanceCommand(vehicule == null ? null : vehicule.id(), 30, false));

    List<Map<String, Object>> lignes = new ArrayList<>();
    List<SourceCopilote> sources = new ArrayList<>();
    for (AnalyseVehicule a : resultat.vehicules().stream().limit(VEHICULES_MAX).toList()) {
      lignes.add(
          ligne(
              "vehicule", a.immatriculation(),
              "score", Math.round(a.score()),
              "statut", a.statut(),
              "kmParJour", a.kmParJour(),
              "consommationL100", a.consommationL100(),
              "kmAvantEcheance", a.kmAvantEcheance(),
              "echeance", a.dateEcheance() == null ? null : a.dateEcheance().toString(),
              "anomalies", a.anomalies(),
              "actions",
                  a.recommandations().stream()
                      .map(
                          r ->
                              r.priorite()
                                  + " : "
                                  + r.libelle()
                                  + (r.avantLe() == null ? "" : " avant le " + r.avantLe())
                                  + (r.dejaPlanifie() ? " (déjà planifié)" : ""))
                      .toList(),
              "explication", a.explication()));
      sources.add(new SourceCopilote("VEHICULE", a.immatriculation(), a.vehiculeId()));
    }
    lignes.add(ligne("synthese", resultat.synthese()));
    return new ResultatOutil(lignes, resultat.vehicules().size(), sources);
  }
}
