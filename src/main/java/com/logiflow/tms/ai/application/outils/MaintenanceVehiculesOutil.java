package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import com.logiflow.tms.shared.domain.DeviseApplication;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class MaintenanceVehiculesOutil implements OutilCopilote {

  private final MaintenanceApi maintenanceApi;
  private final ResolveurEngin resolveurEngin;

  MaintenanceVehiculesOutil(MaintenanceApi maintenanceApi, ResolveurEngin resolveurEngin) {
    this.maintenanceApi = maintenanceApi;
    this.resolveurEngin = resolveurEngin;
  }

  @Override
  public String nom() {
    return "consulter_maintenance";
  }

  @Override
  public String libelle() {
    return "Consultation de la maintenance";
  }

  @Override
  public String description() {
    return "Consulte la maintenance d'un véhicule ou d'une remorque (par immatriculation) ou de "
        + "toute la flotte : ordres de travail (type, nature, origine, statut, dates, coût TTC) et "
        + "plans d'entretien (périodicité km/mois/heures, dernière réalisation, prochaine échéance "
        + "et état OK/ALERTE/ECHU). Chaque ligne indique sa nature : ORDRE_TRAVAIL ou "
        + "PLAN_ENTRETIEN. Pour les sinistres, utiliser rechercher_sinistres.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte(
            "immatriculation", "Immatriculation de l'engin (absent = toute la flotte)"),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    String immatriculation = arguments.texte("immatriculation");
    UUID enginId =
        immatriculation == null ? null : resolveurEngin.parImmatriculation(immatriculation).id();
    var ordres = maintenanceApi.ordresTravail(enginId, arguments.premierePage());
    var plans = maintenanceApi.plansEntretien(enginId, arguments.premierePage());

    var libelles = resolveurEngin.libelles();
    List<Map<String, Object>> lignes = new ArrayList<>();
    List<SourceCopilote> sources = new ArrayList<>();
    for (var ot : ordres.contenu()) {
      lignes.add(
          ligne(
              "nature", "ORDRE_TRAVAIL",
              "reference", ot.reference(),
              "engin", libelles.immatriculation(ot.enginId()),
              "titre", ot.titre(),
              "type", ot.type(),
              "natureIntervention", ot.nature(),
              "origine", ot.origine(),
              "statut", ot.statut(),
              "debutPlanifie", ot.debutPlanifie() == null ? null : ot.debutPlanifie().toString(),
              "finReelle", ot.finReelle() == null ? null : ot.finReelle().toString(),
              "coutTtc", ot.totalTtc(),
              "devise", DeviseApplication.PAR_DEFAUT.getCurrencyCode()));
      sources.add(new SourceCopilote("ORDRE_TRAVAIL", ot.reference(), ot.id().toString()));
    }
    for (var plan : plans.contenu()) {
      lignes.add(
          ligne(
              "nature", "PLAN_ENTRETIEN",
              "engin", libelles.immatriculation(plan.enginId()),
              "libelle", plan.libelle(),
              "type", plan.type(),
              "periodiciteKm", plan.periodiciteKm(),
              "periodiciteMois", plan.periodiciteMois(),
              "periodiciteHeures", plan.periodiciteHeures(),
              "derniereRealisation",
                  plan.derniereDate() == null ? null : plan.derniereDate().toString(),
              "derniereKm", plan.derniereKm(),
              "kmRestant", plan.kmRestant(),
              "dateEcheance", plan.dateEcheance() == null ? null : plan.dateEcheance().toString(),
              "etat", plan.etat()));
    }
    sources.addAll(libelles.sources());
    return new ResultatOutil(lignes, ordres.totalElements() + plans.totalElements(), sources);
  }
}
