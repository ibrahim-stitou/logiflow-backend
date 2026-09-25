package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class ListerVehiculesOutil implements OutilCopilote {

  private final VehiculeApi vehiculeApi;
  private final MaintenanceApi maintenanceApi;
  private final Clock horloge;

  ListerVehiculesOutil(VehiculeApi vehiculeApi, MaintenanceApi maintenanceApi, Clock horloge) {
    this.vehiculeApi = vehiculeApi;
    this.maintenanceApi = maintenanceApi;
    this.horloge = horloge;
  }

  @Override
  public String nom() {
    return "lister_vehicules";
  }

  @Override
  public String libelle() {
    return "Consultation de la flotte de véhicules";
  }

  @Override
  public String description() {
    return "Liste les véhicules (tracteurs, porteurs) filtrés par immatriculation et/ou statut "
        + "(ex. DISPONIBLE pour les véhicules libres). Renvoie immatriculation, type, statut, PTAC "
        + "et charge utile (kg), validité des documents réglementaires à ce jour et dernier score "
        + "de santé (maintenance prédictive), ainsi que le nombre total de véhicules "
        + "correspondants.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte("immatriculation", "Immatriculation complète ou partielle"),
        SchemaOutil.enumere("statut", "Statut du véhicule", vehiculeApi.statutsConnus()),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    var page =
        vehiculeApi.rechercher(
            arguments.texte("immatriculation"),
            arguments.enumere("statut", vehiculeApi.statutsConnus()),
            arguments.premierePage());
    LocalDate aujourdhui = LocalDate.now(horloge);
    return new ResultatOutil(
        page.contenu().stream().map(v -> versLigne(v, aujourdhui)).toList(),
        page.totalElements(),
        page.contenu().stream()
            .map(v -> new SourceCopilote("VEHICULE", v.immatriculation(), v.id().toString()))
            .toList());
  }

  private Map<String, Object> versLigne(VehiculeSummary vehicule, LocalDate aujourdhui) {
    var score = maintenanceApi.dernierScoreSante(vehicule.id()).orElse(null);
    return ligne(
        "immatriculation", vehicule.immatriculation(),
        "type", vehicule.type(),
        "statut", vehicule.statut(),
        "ptacKg", vehicule.ptacKg(),
        "chargeUtileKg", vehicule.chargeUtileKg(),
        "documentsValides", vehiculeApi.documentsValides(vehicule.id(), aujourdhui),
        "scoreSante",
            score == null
                ? null
                : ligne(
                    "score", score.score(),
                    "statut", score.statut(),
                    "necessiteIntervention", score.necessiteIntervention(),
                    "calculeLe", score.calculeLe().toString()));
  }
}
