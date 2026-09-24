package com.logiflow.tms.driver.infrastructure.web.dto;

import com.logiflow.tms.driver.domain.vo.Habilitation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Requête de mise à jour d'un chauffeur : remplace nom, prénom, profil et habilitations. Le
 * matricule n'est pas modifiable ; statut et disponibilité ont leurs propres endpoints.
 */
public record ChauffeurMajRequest(
    @NotBlank @Size(max = 100) String nom,
    @NotBlank @Size(max = 100) String prenom,
    @Valid ProfilChauffeurRequest profil,
    List<@Valid Habilitation> habilitations) {}
