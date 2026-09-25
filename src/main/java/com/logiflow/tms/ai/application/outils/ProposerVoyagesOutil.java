package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.application.PlanificationVoyageService;
import com.logiflow.tms.ai.application.PlanificationVoyageService.OptionValidee;
import com.logiflow.tms.ai.application.PlanificationVoyageService.PropositionsVoyage;
import com.logiflow.tms.ai.application.command.ProposerVoyagesCommand;
import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification.Indicateurs;
import com.logiflow.tms.shared.domain.DeviseApplication;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Outil copilote de l'agent de planification : résume les voyages proposés sur une période. La
 * création reste une action de l'exploitant, depuis l'écran de planification (lien en source).
 */
@Component
class ProposerVoyagesOutil implements OutilCopilote {

  private static final ZoneId FUSEAU_EXPLOITATION = ZoneId.of("Europe/Paris");
  private static final List<String> TYPES =
      List.of("SIMPLE", "GROUPAGE", "RAMASSE", "DISTRIBUTION", "NAVETTE");
  private static final List<String> PORTEES = List.of("NATIONAL", "INTERNATIONAL");

  private final PlanificationVoyageService planificationVoyageService;

  ProposerVoyagesOutil(PlanificationVoyageService planificationVoyageService) {
    this.planificationVoyageService = planificationVoyageService;
  }

  @Override
  public String nom() {
    return "proposer_voyages";
  }

  @Override
  public String libelle() {
    return "Planification des voyages";
  }

  @Override
  public String description() {
    return "Demande à l'agent de planification des propositions de voyages sur une période : "
        + "dossiers à grouper, ordre des arrêts avec heures d'arrivée, tracteur et remorque, "
        + "chauffeurs, indicateurs (km, remplissage, coût) et conformité. Ne crée rien : "
        + "l'exploitant choisit et crée le voyage depuis l'écran de planification. "
        + "Par défaut : de demain à J+7, type GROUPAGE, portée NATIONAL.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.date("debut", "Premier jour de la période (défaut : demain)"),
        SchemaOutil.date("fin", "Dernier jour inclus de la période (défaut : début + 6 jours)"),
        SchemaOutil.enumere("typeVoyage", "Type de voyage", TYPES),
        SchemaOutil.enumere("portee", "Portée", PORTEES));
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    LocalDate debut = arguments.date("debut");
    if (debut == null) {
      debut = LocalDate.now(FUSEAU_EXPLOITATION).plusDays(1);
    }
    LocalDate fin = arguments.date("fin");
    if (fin == null || fin.isBefore(debut)) {
      fin = debut.plusDays(6);
    }
    String type = arguments.enumere("typeVoyage", TYPES);
    String portee = arguments.enumere("portee", PORTEES);
    PropositionsVoyage propositions =
        planificationVoyageService.proposer(
            new ProposerVoyagesCommand(
                debut.atStartOfDay(FUSEAU_EXPLOITATION).toInstant(),
                fin.plusDays(1).atStartOfDay(FUSEAU_EXPLOITATION).toInstant(),
                type == null ? "GROUPAGE" : type,
                portee == null ? "NATIONAL" : portee,
                3));

    List<Map<String, Object>> lignes = new ArrayList<>();
    Set<SourceCopilote> sources = new LinkedHashSet<>();
    for (OptionValidee validee : propositions.options()) {
      lignes.add(versLigne(validee, propositions, sources));
    }
    lignes.add(
        ligne(
            "synthese",
            propositions.comparaison(),
            "dossiersCandidats",
            propositions.nbDossiersCandidats(),
            "dossiersNonPlanifiables",
            propositions.dossiersNonPlanifiables()));
    sources.add(new SourceCopilote("PLANIFICATION", "Ouvrir la planification des voyages", null));
    return new ResultatOutil(lignes, propositions.options().size(), List.copyOf(sources));
  }

  private static Map<String, Object> versLigne(
      OptionValidee validee, PropositionsVoyage propositions, Set<SourceCopilote> sources) {
    var option = validee.option();
    Indicateurs i = option.indicateurs();
    List<String> dossiers =
        option.dossierIds().stream()
            .map(id -> propositions.libellesDossiers().getOrDefault(id, id))
            .toList();
    option
        .dossierIds()
        .forEach(
            id ->
                sources.add(
                    new SourceCopilote(
                        "DOSSIER", propositions.libellesDossiers().getOrDefault(id, id), id)));
    return ligne(
        "option",
        option.rang(),
        "objectif",
        option.libelleObjectif(),
        "recommandee",
        option.recommandee(),
        "conforme",
        validee.conformite().conforme(),
        "dossiers",
        dossiers,
        "arrets",
        option.arrets().stream().map(a -> a.libelle()).toList(),
        "vehicule",
        propositions.libellesVehicules().getOrDefault(option.vehiculeId(), option.vehiculeId()),
        "remorque",
        option.remorqueId() == null
            ? null
            : propositions
                .libellesRemorques()
                .getOrDefault(option.remorqueId(), option.remorqueId()),
        "chauffeurs",
        option.chauffeurIds().stream()
            .map(id -> propositions.libellesChauffeurs().getOrDefault(id, id))
            .toList(),
        "depart",
        option.departPrevu().toString(),
        "arrivee",
        option.arriveePrevue().toString(),
        "distanceKm",
        i.distanceKm(),
        "remplissagePct",
        Math.round(Math.max(i.tauxRemplissagePoids(), i.tauxRemplissageVolume()) * 100),
        "coutEstime",
        Math.round(i.coutEstime()),
        "devise",
        DeviseApplication.PAR_DEFAUT.getCurrencyCode(),
        "alertes",
        option.alertes(),
        "anomaliesBloquantes",
        validee.conformite().bloquants(),
        "justification",
        option.justification());
  }
}
