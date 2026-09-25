package com.logiflow.tms.driver.api.dto;

import java.time.LocalDate;

/**
 * Exigences d'un voyage envers un chauffeur, pour {@code ChauffeurApi.motifsNonAffectation}. {@code
 * permisRequis} : nom de catégorie (C, CE…) ou null si non vérifiée.
 */
public record ExigencesAffectationDto(
    LocalDate date, boolean adr, boolean international, String permisRequis) {}
