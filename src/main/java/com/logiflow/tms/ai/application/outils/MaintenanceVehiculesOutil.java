package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class MaintenanceVehiculesOutil implements OutilCopilote {

  private final MaintenanceApi maintenanceApi;
  private final VehiculeApi vehiculeApi;
  private final ResolveurVehicule resolveurVehicule;

  MaintenanceVehiculesOutil(
      MaintenanceApi maintenanceApi, VehiculeApi vehiculeApi, ResolveurVehicule resolveurVehicule) {
    this.maintenanceApi = maintenanceApi;
    this.vehiculeApi = vehiculeApi;
    this.resolveurVehicule = resolveurVehicule;
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
    return "Consulte la maintenance d'un véhicule (par immatriculation) ou de toute la flotte : "
        + "ordres de travail (type d'intervention, statut, date planifiée, coût) et plans "
        + "d'entretien préventif (périodicité en km/mois). Chaque ligne indique sa nature : "
        + "ORDRE_TRAVAIL ou PLAN_ENTRETIEN.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte(
            "immatriculation", "Immatriculation du véhicule (absent = toute la flotte)"),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    String immatriculation = arguments.texte("immatriculation");
    UUID vehiculeId =
        immatriculation == null ? null : resolveurVehicule.parImmatriculation(immatriculation).id();
    var ordres = maintenanceApi.ordresTravail(vehiculeId, arguments.premierePage());
    var plans = maintenanceApi.plansEntretien(vehiculeId, arguments.premierePage());

    Map<UUID, String> immatriculations = new HashMap<>();
    List<Map<String, Object>> lignes = new ArrayList<>();
    for (var ot : ordres.contenu()) {
      lignes.add(
          ligne(
              "nature", "ORDRE_TRAVAIL",
              "vehicule", immatriculation(ot.vehiculeId(), immatriculations),
              "type", ot.type(),
              "statut", ot.statut(),
              "datePlanifiee", ot.datePlanifiee() == null ? null : ot.datePlanifiee().toString(),
              "cout", ot.cout(),
              "devise", ot.devise()));
    }
    for (var plan : plans.contenu()) {
      lignes.add(
          ligne(
              "nature", "PLAN_ENTRETIEN",
              "vehicule", immatriculation(plan.vehiculeId(), immatriculations),
              "libelle", plan.libelle(),
              "periodiciteKm", plan.periodiciteKm(),
              "periodiciteMois", plan.periodiciteMois(),
              "seuilAlerteKm", plan.seuilAlerteKm()));
    }
    List<SourceCopilote> sources =
        immatriculations.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> new SourceCopilote("VEHICULE", e.getValue(), e.getKey().toString()))
            .toList();
    return new ResultatOutil(lignes, ordres.totalElements() + plans.totalElements(), sources);
  }

  private String immatriculation(UUID vehiculeId, Map<UUID, String> cache) {
    if (vehiculeId == null) {
      return null;
    }
    return cache.computeIfAbsent(
        vehiculeId,
        id -> vehiculeApi.consulter(id).map(VehiculeSummary::immatriculation).orElse(null));
  }
}
