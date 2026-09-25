package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.application.MaintenancePredictiveService;
import com.logiflow.tms.ai.application.MaintenancePredictiveService.AnalyserMaintenanceCommand;
import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance.AnalyseVehicule;
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
  private final ResolveurEngin resolveurEngin;

  AnalyserMaintenanceOutil(MaintenancePredictiveService service, ResolveurEngin resolveurEngin) {
    this.service = service;
    this.resolveurEngin = resolveurEngin;
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
    return "Analyse prédictive de la maintenance d'un véhicule ou d'une remorque (par "
        + "immatriculation) ou de toute la flotte sur les 30 prochains jours : score de santé, "
        + "échéances des plans d'entretien avancées par l'usage réel et les voyages planifiés, "
        + "documents à renouveler, pannes récurrentes, sinistralité, surconsommation, actions "
        + "recommandées et créneau libre proposé. Pour la flotte, renvoie les engins les plus à "
        + "risque d'abord.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte(
            "immatriculation", "Immatriculation de l'engin (absent = toute la flotte)"));
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    String immatriculation = arguments.texte("immatriculation");
    ResolveurEngin.Engin engin =
        immatriculation == null ? null : resolveurEngin.parImmatriculation(immatriculation);
    ResultatMaintenance resultat =
        service.analyser(
            new AnalyserMaintenanceCommand(engin == null ? null : engin.id(), 30, false));

    List<Map<String, Object>> lignes = new ArrayList<>();
    List<SourceCopilote> sources = new ArrayList<>();
    for (AnalyseVehicule a : resultat.vehicules().stream().limit(VEHICULES_MAX).toList()) {
      lignes.add(
          ligne(
              "engin", a.immatriculation(),
              "typeEngin", a.typeEngin(),
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
      sources.add(
          new SourceCopilote(
              a.typeEngin() == null ? "VEHICULE" : a.typeEngin(),
              a.immatriculation(),
              a.vehiculeId()));
    }
    lignes.add(ligne("synthese", resultat.synthese()));
    return new ResultatOutil(lignes, resultat.vehicules().size(), sources);
  }
}
