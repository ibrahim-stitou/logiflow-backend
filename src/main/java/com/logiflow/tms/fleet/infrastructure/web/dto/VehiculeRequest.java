package com.logiflow.tms.fleet.infrastructure.web.dto;

import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.domain.vo.DocumentVehicule;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/** Requête de création d'un véhicule. */
public record VehiculeRequest(
    @NotBlank String immatriculation,
    @NotNull TypeVehicule type,
    @Positive double ptacKg,
    @Positive double chargeUtileKg,
    List<@Valid DocumentVehicule> documents) {}
