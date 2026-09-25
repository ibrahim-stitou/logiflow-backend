package com.logiflow.tms.fleet.api.dto;

import java.util.UUID;

/** État d'une remorque pour la maintenance : compteurs (km, heures groupe froid) et âge. */
public record RemorqueEtatSummary(
    UUID id,
    String immatriculation,
    String carrosserie,
    String statut,
    int kilometrage,
    int heuresGroupeFroid,
    Integer anneeFabrication) {}
