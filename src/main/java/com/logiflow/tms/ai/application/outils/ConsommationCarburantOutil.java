package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.carburant.api.CarburantApi;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class ConsommationCarburantOutil implements OutilCopilote {

  private final CarburantApi carburantApi;
  private final ResolveurVehicule resolveurVehicule;
  private final Clock horloge;

  ConsommationCarburantOutil(
      CarburantApi carburantApi, ResolveurVehicule resolveurVehicule, Clock horloge) {
    this.carburantApi = carburantApi;
    this.resolveurVehicule = resolveurVehicule;
    this.horloge = horloge;
  }

  @Override
  public String nom() {
    return "consommation_carburant";
  }

  @Override
  public String libelle() {
    return "Calcul de la consommation de carburant";
  }

  @Override
  public String description() {
    return "Calcule la consommation de carburant (nombre de prises, litres, montant TTC, détail "
        + "par type de carburant) sur une période, pour un véhicule (par immatriculation) ou "
        + "toute la flotte. Par défaut : du 1er du mois en cours à aujourd'hui.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.date("debut", "Premier jour de la période, inclus"),
        SchemaOutil.date("fin", "Dernier jour de la période, inclus"),
        SchemaOutil.texte(
            "immatriculation", "Immatriculation du véhicule (absent = toute la flotte)"));
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION_ET_ATELIER;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    LocalDate aujourdhui = LocalDate.now(horloge);
    LocalDate fin = arguments.date("fin") != null ? arguments.date("fin") : aujourdhui;
    LocalDate debut =
        arguments.date("debut") != null ? arguments.date("debut") : fin.withDayOfMonth(1);
    if (debut.isAfter(fin)) {
      throw new ValidationException(
          "La date de début doit précéder la date de fin.", List.of("debut: après fin"));
    }
    String immatriculation = arguments.texte("immatriculation");
    VehiculeSummary vehicule =
        immatriculation == null ? null : resolveurVehicule.parImmatriculation(immatriculation);

    var conso = carburantApi.consommation(vehicule == null ? null : vehicule.id(), debut, fin);
    return new ResultatOutil(
        List.of(
            ligne(
                "vehicule", vehicule == null ? "toute la flotte" : vehicule.immatriculation(),
                "debut", debut.toString(),
                "fin", fin.toString(),
                "nombrePrises", conso.nombrePrises(),
                "litres", conso.litresTotal(),
                "montantTtc", conso.montantTotalTtc(),
                "parType",
                    conso.parType().stream()
                        .map(
                            t ->
                                ligne(
                                    "type", t.typeCarburant(),
                                    "nombrePrises", t.nombrePrises(),
                                    "litres", t.litres(),
                                    "montantTtc", t.montantTtc()))
                        .toList())),
        1,
        vehicule == null
            ? List.of()
            : List.of(
                new SourceCopilote(
                    "VEHICULE", vehicule.immatriculation(), vehicule.id().toString())));
  }
}
