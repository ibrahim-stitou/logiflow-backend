package com.logiflow.tms.driver.infrastructure.web.dto;

import com.logiflow.tms.driver.domain.model.TypeContrat;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.List;

/**
 * Requête de création ou de mise à jour d'un chauffeur. Le matricule est ignoré à la mise à jour.
 */
public record ChauffeurRequest(
    @NotBlank String matricule,
    @NotBlank String nom,
    @NotBlank String prenom,
    String cin,
    LocalDate dateNaissance,
    String lieuNaissance,
    String nationalite,
    String telephone,
    String email,
    String adresse,
    String numeroPermis,
    String categoriePermis,
    LocalDate dateObtentionPermis,
    LocalDate dateExpirationPermis,
    String numeroPasseport,
    LocalDate dateDelivrancePasseport,
    LocalDate dateExpirationPasseport,
    String paysDelivrancePasseport,
    String numeroVisa,
    String typeVisa,
    String paysVisa,
    LocalDate dateDelivranceVisa,
    LocalDate dateExpirationVisa,
    LocalDate dateEmbauche,
    TypeContrat typeContrat,
    Integer experienceAnnees,
    String specialisation,
    List<@Valid Habilitation> habilitations,
    @PositiveOrZero long soldeTempsConduiteInitialMinutes) {}
