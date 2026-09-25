package com.logiflow.tms.planning.api.dto;

import java.util.Set;
import java.util.UUID;

/** Ressources déjà engagées sur un voyage actif pendant une période. */
public record RessourcesOccupeesSummary(
    Set<UUID> vehicules, Set<UUID> remorques, Set<UUID> chauffeurs) {}
