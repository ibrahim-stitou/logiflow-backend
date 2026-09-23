package com.logiflow.tms.carburant.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record StationMajRequest(@NotBlank String libelle, String adresse) {}
