package com.logiflow.tms.iam.api.dto;

import java.util.Set;
import java.util.UUID;

/** Vue publique et minimale d'un utilisateur, exposée aux autres modules. */
public record UtilisateurSummary(
    UUID id, String login, String email, Set<String> roles, boolean actif) {}
