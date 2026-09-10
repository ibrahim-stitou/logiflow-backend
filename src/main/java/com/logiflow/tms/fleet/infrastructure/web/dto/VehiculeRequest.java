package com.logiflow.tms.fleet.infrastructure.web.dto;

import com.logiflow.tms.fleet.domain.model.Energie;
import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/** Requête de création d'un véhicule. */
public record VehiculeRequest(
    @NotBlank String immatriculation,
    @NotNull TypeVehicule type,
    String numeroParc,
    String vin,
    String marque,
    String modele,
    Integer anneeMiseEnCirculation,
    Energie energie,
    @Positive double ptacKg,
    Double poidsVideKg,
    @Positive double chargeUtileKg,
    Double longueurM,
    Double largeurM,
    Double hauteurM,
    Double volumeUtileM3,
    Integer nbPositionsPalettes,
    TypeCarrosserie typeCarrosserie,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax,
    LocalDate datePremiereMiseCirculation,
    LocalDate dateAcquisition,
    LocalDate dateMiseEnService) {}
