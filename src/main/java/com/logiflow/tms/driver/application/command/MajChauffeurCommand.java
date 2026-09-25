package com.logiflow.tms.driver.application.command;

import com.logiflow.tms.driver.domain.model.ProfilChauffeur;
import com.logiflow.tms.driver.domain.vo.Habilitation;
import java.util.List;

/**
 * Commande applicative de mise à jour d'un chauffeur existant : tout sauf le matricule (non
 * modifiable), le statut et la disponibilité (changements dédiés) et le solde de conduite.
 */
public record MajChauffeurCommand(
    String nom, String prenom, ProfilChauffeur profil, List<Habilitation> habilitations) {}
