package com.logiflow.tms.planning.infrastructure.web.dto;

import com.logiflow.tms.planning.application.command.CreerVoyageCommand;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Trajet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Requête de création d'un voyage (ressources déjà choisies, contrôlées par le moteur de
 * conformité). {@code arrets} (optionnel) impose l'ordre de passage par site ; absent, les arrêts
 * sont déduits des fenêtres horaires des dossiers.
 */
public record VoyageRequest(
    @NotNull TypeVoyage typeVoyage,
    @NotNull Portee portee,
    @NotNull Instant departPrevu,
    @NotNull Instant arriveePrevue,
    @NotNull UUID vehiculeId,
    UUID remorqueId,
    @NotEmpty List<UUID> dossierIds,
    @NotNull @Valid Trajet trajet,
    @NotEmpty List<@Valid Affectation> affectations,
    List<@Valid ArretRequest> arrets) {

  /** Arrêt imposé, identifié par son site du référentiel. */
  public record ArretRequest(@NotNull UUID siteId) {}

  public VoyageRequest(
      TypeVoyage typeVoyage,
      Portee portee,
      Instant departPrevu,
      Instant arriveePrevue,
      UUID vehiculeId,
      UUID remorqueId,
      List<UUID> dossierIds,
      Trajet trajet,
      List<Affectation> affectations) {
    this(
        typeVoyage,
        portee,
        departPrevu,
        arriveePrevue,
        vehiculeId,
        remorqueId,
        dossierIds,
        trajet,
        affectations,
        null);
  }

  public CreerVoyageCommand versCommande() {
    return new CreerVoyageCommand(
        typeVoyage,
        portee,
        departPrevu,
        arriveePrevue,
        vehiculeId,
        remorqueId,
        dossierIds,
        trajet,
        affectations,
        arrets == null || arrets.isEmpty()
            ? null
            : arrets.stream().map(ArretRequest::siteId).toList());
  }
}
