package com.logiflow.tms.planning.infrastructure.web.dto;

import com.logiflow.tms.planning.application.command.AjouterDossierVoyageCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Corps de requête pour {@code POST /api/v1/voyages/{voyageId}/dossiers}. */
public record AjouterDossierVoyageRequest(
    @NotNull UUID dossierId,
    @NotNull @Valid SelectionArretRequest chargement,
    @NotNull @Valid SelectionArretRequest dechargement,
    Double deviationMaxPourcent) {

  public record SelectionArretRequest(UUID arretId, @Valid NouvelArretRequest nouvelArret) {}

  public record NouvelArretRequest(@NotNull String libelle, double latitude, double longitude) {}

  public AjouterDossierVoyageCommand versCommande() {
    return new AjouterDossierVoyageCommand(
        dossierId, versSelection(chargement), versSelection(dechargement), deviationMaxPourcent);
  }

  private static AjouterDossierVoyageCommand.SelectionArret versSelection(
      SelectionArretRequest selection) {
    AjouterDossierVoyageCommand.NouvelArret nouvel =
        selection.nouvelArret() == null
            ? null
            : new AjouterDossierVoyageCommand.NouvelArret(
                selection.nouvelArret().libelle(),
                selection.nouvelArret().latitude(),
                selection.nouvelArret().longitude());
    return new AjouterDossierVoyageCommand.SelectionArret(selection.arretId(), nouvel);
  }
}
