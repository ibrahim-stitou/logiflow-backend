package com.logiflow.tms.driver.application.command;

import com.logiflow.tms.driver.domain.vo.Habilitation;
import java.util.List;

/** Commande applicative de création d'un chauffeur. */
public record CreerChauffeurCommand(
    String matricule,
    String nomComplet,
    List<Habilitation> habilitations,
    long soldeTempsConduiteInitialMinutes) {}
