package com.logiflow.tms.iam.application.command;

import com.logiflow.tms.iam.domain.model.RoleUtilisateur;
import java.util.Set;

/** Commande applicative de création d'un utilisateur. */
public record CreerUtilisateurCommand(String login, String email, Set<RoleUtilisateur> roles) {}
