package com.logiflow.tms.planning.infrastructure.web.dto;

import com.logiflow.tms.planning.application.command.CreerVoyageCommand;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.planning.infrastructure.web.dto.VoyageRequest.ArretRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Projet de voyage, éventuellement incomplet, soumis au contrôle de conformité à blanc : les
 * éléments manquants sont signalés comme anomalies au lieu d'être refusés.
 */
public record ConformiteVoyageRequest(
    TypeVoyage typeVoyage,
    Portee portee,
    Instant departPrevu,
    Instant arriveePrevue,
    UUID vehiculeId,
    UUID remorqueId,
    List<UUID> dossierIds,
    @Valid Trajet trajet,
    List<@Valid Affectation> affectations,
    List<@Valid ArretRequest> arrets) {

  public CreerVoyageCommand versCommande() {
    return new CreerVoyageCommand(
        typeVoyage == null ? TypeVoyage.SIMPLE : typeVoyage,
        portee == null ? Portee.NATIONAL : portee,
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
