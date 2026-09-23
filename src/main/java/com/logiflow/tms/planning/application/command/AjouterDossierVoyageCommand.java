package com.logiflow.tms.planning.application.command;

import java.util.UUID;

/** Commande d'ajout d'un dossier existant à un voyage avec résolution des arrêts. */
public record AjouterDossierVoyageCommand(
    UUID dossierId,
    SelectionArret chargement,
    SelectionArret dechargement,
    Double deviationMaxPourcent) {

  public record SelectionArret(UUID arretExistantId, NouvelArret nouvelArret) {}

  public record NouvelArret(String libelle, double latitude, double longitude) {}
}
