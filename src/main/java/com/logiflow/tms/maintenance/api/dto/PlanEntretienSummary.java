package com.logiflow.tms.maintenance.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Vue publique d'un plan d'entretien avec sa prochaine échéance ({@code etat} : OK, ALERTE ou ECHU
 * ; kilomètres et date null quand la périodicité correspondante n'existe pas).
 */
public record PlanEntretienSummary(
    UUID id,
    String typeEngin,
    UUID enginId,
    String libelle,
    String type,
    Integer periodiciteKm,
    Integer periodiciteMois,
    Integer periodiciteHeures,
    int seuilAlerteKm,
    int dureeEstimeeMin,
    LocalDate derniereDate,
    Integer derniereKm,
    Integer kmRestant,
    LocalDate dateEcheance,
    String etat) {}
