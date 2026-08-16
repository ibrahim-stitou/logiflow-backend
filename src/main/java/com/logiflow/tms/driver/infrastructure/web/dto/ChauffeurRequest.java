package com.logiflow.tms.driver.infrastructure.web.dto;

import com.logiflow.tms.driver.domain.vo.Habilitation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * Requête de création ou de mise à jour d'un chauffeur. Le matricule est ignoré à la mise à jour.
 */
public record ChauffeurRequest(
    @NotBlank String matricule,
    @NotBlank String nomComplet,
    List<@Valid Habilitation> habilitations,
    @PositiveOrZero long soldeTempsConduiteInitialMinutes) {}
