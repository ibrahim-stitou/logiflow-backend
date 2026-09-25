package com.logiflow.tms.fleet.api.dto;

import java.util.UUID;

/** État d'un véhicule pour l'analyse de maintenance : compteurs et âge. */
public record VehiculeEtatSummary(
    UUID id,
    String immatriculation,
    String type,
    String statut,
    int kilometrage,
    int heuresMoteur,
    Integer anneeMiseEnCirculation) {}
