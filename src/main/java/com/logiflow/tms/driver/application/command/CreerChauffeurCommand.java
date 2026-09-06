package com.logiflow.tms.driver.application.command;

import com.logiflow.tms.driver.domain.model.TypeContrat;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import java.time.LocalDate;
import java.util.List;

/** Commande applicative de création d'un chauffeur. */
public record CreerChauffeurCommand(
    String matricule,
    String nom,
    String prenom,
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
    List<Habilitation> habilitations,
    long soldeTempsConduiteInitialMinutes) {}
