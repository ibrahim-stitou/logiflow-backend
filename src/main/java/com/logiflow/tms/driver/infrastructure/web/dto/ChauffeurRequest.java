package com.logiflow.tms.driver.infrastructure.web.dto;

import com.logiflow.tms.driver.domain.vo.Habilitation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Requête de création d'un chauffeur. Le profil administratif est entièrement optionnel. */
public record ChauffeurRequest(
    @NotBlank @Size(max = 30) String matricule,
    @NotBlank @Size(max = 100) String nom,
    @NotBlank @Size(max = 100) String prenom,
    @Valid ProfilChauffeurRequest profil,
    List<@Valid Habilitation> habilitations,
    @PositiveOrZero long soldeTempsConduiteInitialMinutes) {}
