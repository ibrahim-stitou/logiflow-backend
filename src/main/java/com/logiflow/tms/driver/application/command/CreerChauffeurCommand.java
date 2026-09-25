package com.logiflow.tms.driver.application.command;

import com.logiflow.tms.driver.domain.model.ProfilChauffeur;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import java.util.List;

/** Commande applicative de création d'un chauffeur. */
public record CreerChauffeurCommand(
    String matricule,
    String nom,
    String prenom,
    ProfilChauffeur profil,
    List<Habilitation> habilitations,
    long soldeTempsConduiteInitialMinutes) {}
