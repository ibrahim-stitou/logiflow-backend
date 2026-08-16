package com.logiflow.tms.planning.infrastructure.web.dto;

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
 * conformité).
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
    @NotEmpty List<@Valid Affectation> affectations) {}
