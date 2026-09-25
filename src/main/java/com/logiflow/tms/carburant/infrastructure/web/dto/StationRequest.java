package com.logiflow.tms.carburant.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record StationRequest(@NotBlank String code, @NotBlank String libelle, String adresse) {}
