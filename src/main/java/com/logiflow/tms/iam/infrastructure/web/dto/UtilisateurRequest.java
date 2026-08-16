package com.logiflow.tms.iam.infrastructure.web.dto;

import com.logiflow.tms.iam.domain.model.RoleUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/** Requête de création ou de mise à jour d'un utilisateur. Le login est ignoré à la mise à jour. */
public record UtilisateurRequest(
    @NotBlank String login, @NotBlank @Email String email, @NotEmpty Set<RoleUtilisateur> roles) {}
