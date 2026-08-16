package com.logiflow.tms.iam.application.command;

import com.logiflow.tms.iam.domain.model.RoleUtilisateur;
import java.util.Set;

/**
 * Commande applicative de mise à jour d'un utilisateur existant (le login n'est pas modifiable).
 */
public record MajUtilisateurCommand(String email, Set<RoleUtilisateur> roles) {}
