package com.logiflow.tms.ai.application.outils;

import static com.logiflow.tms.ai.application.outils.Lignes.ligne;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorqueSummary;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.planning.api.dto.VoyageSummary;
import com.logiflow.tms.tracking.api.TrackingApi;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
class RechercherVoyagesOutil implements OutilCopilote {

  private final VoyageApi voyageApi;
  private final VehiculeApi vehiculeApi;
  private final RemorqueApi remorqueApi;
  private final TrackingApi trackingApi;

  RechercherVoyagesOutil(
      VoyageApi voyageApi,
      VehiculeApi vehiculeApi,
      RemorqueApi remorqueApi,
      TrackingApi trackingApi) {
    this.voyageApi = voyageApi;
    this.vehiculeApi = vehiculeApi;
    this.remorqueApi = remorqueApi;
    this.trackingApi = trackingApi;
  }

  @Override
  public String nom() {
    return "rechercher_voyages";
  }

  @Override
  public String libelle() {
    return "Recherche des voyages";
  }

  @Override
  public String description() {
    return "Recherche les voyages (tournées) par référence (ex. VOY-2026-00003) et/ou statut, du "
        + "plus récent au plus ancien. Renvoie pour chacun le statut, le véhicule et la remorque "
        + "(immatriculations), le nombre de dossiers transportés et le dernier événement de suivi "
        + "(type et horodatage), ainsi que le nombre total de voyages correspondants.";
  }

  @Override
  public Map<String, Object> parametres() {
    return SchemaOutil.objet(
        SchemaOutil.texte("reference", "Référence complète ou partielle du voyage"),
        SchemaOutil.enumere("statut", "Statut du voyage", voyageApi.statutsConnus()),
        SchemaOutil.limite());
  }

  @Override
  public Set<String> rolesAutorises() {
    return RolesCopilote.EXPLOITATION;
  }

  @Override
  public ResultatOutil executer(ArgumentsOutil arguments) {
    var page =
        voyageApi.rechercher(
            arguments.texte("reference"),
            arguments.enumere("statut", voyageApi.statutsConnus()),
            arguments.premierePage());
    return new ResultatOutil(
        page.contenu().stream().map(this::versLigne).toList(),
        page.totalElements(),
        page.contenu().stream()
            .map(v -> new SourceCopilote("VOYAGE", v.reference(), v.id().toString()))
            .toList());
  }

  private Map<String, Object> versLigne(VoyageSummary voyage) {
    var dernierEvenement = trackingApi.dernierEvenement(voyage.id()).orElse(null);
    return ligne(
        "reference",
        voyage.reference(),
        "statut",
        voyage.statut(),
        "vehicule",
        voyage.vehiculeId() == null
            ? null
            : vehiculeApi
                .consulter(voyage.vehiculeId())
                .map(VehiculeSummary::immatriculation)
                .orElse(null),
        "remorque",
        voyage.remorqueId() == null
            ? null
            : remorqueApi
                .consulter(voyage.remorqueId())
                .map(RemorqueSummary::immatriculation)
                .orElse(null),
        "nbDossiers",
        voyage.dossierIds().size(),
        "dernierEvenement",
        dernierEvenement == null
            ? null
            : ligne(
                "type", dernierEvenement.type(),
                "horodatage", dernierEvenement.horodatage().toString()));
  }
}
