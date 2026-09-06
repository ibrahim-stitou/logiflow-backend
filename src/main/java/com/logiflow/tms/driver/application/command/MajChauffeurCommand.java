package com.logiflow.tms.driver.application.command;

import com.logiflow.tms.driver.domain.vo.Habilitation;
import java.util.List;

/**
 * Commande applicative de mise à jour d'un chauffeur existant (le matricule n'est pas modifiable).
 */
public record MajChauffeurCommand(String nom, String prenom, List<Habilitation> habilitations) {}
