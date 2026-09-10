package com.logiflow.tms.driver.api.dto;

import java.util.UUID;

/** Vue publique et minimale d'un chauffeur, exposée aux autres modules (planning). */
public record ChauffeurSummary(
    UUID id,
    String matricule,
    String nom,
    String prenom,
    String statut,
    String disponibilite,
    long soldeTempsConduiteMinutes) {}
