package com.logiflow.tms.maintenance.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Vue publique et minimale du dernier score de santé d'un véhicule. */
public record ScoreSanteSummary(
    UUID vehiculeId,
    LocalDate calculeLe,
    double score,
    String statut,
    boolean necessiteIntervention) {}
