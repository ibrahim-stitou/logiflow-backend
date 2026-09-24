package com.logiflow.tms.planning.application.command;

import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Trajet;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Commande applicative de création d'un voyage, ressources déjà choisies (affectation directe).
 *
 * <p>{@code ordreSites} (optionnel) impose l'ordre des arrêts par site ; absent, les arrêts sont
 * déduits des fenêtres horaires des dossiers. Pour un contrôle de conformité à blanc, les
 * ressources et le trajet peuvent être absents : l'absence est alors signalée comme anomalie.
 */
public record CreerVoyageCommand(
    TypeVoyage typeVoyage,
    Portee portee,
    Instant departPrevu,
    Instant arriveePrevue,
    UUID vehiculeId,
    UUID remorqueId,
    List<UUID> dossierIds,
    Trajet trajet,
    List<Affectation> affectations,
    List<UUID> ordreSites) {

  public CreerVoyageCommand {
    dossierIds = dossierIds == null ? List.of() : List.copyOf(dossierIds);
    affectations = affectations == null ? List.of() : List.copyOf(affectations);
    ordreSites = ordreSites == null ? null : List.copyOf(ordreSites);
  }

  public CreerVoyageCommand(
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
}
