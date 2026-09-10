package com.logiflow.tms.fleet.infrastructure.web.dto;

import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.domain.model.TypeRemorque;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/** Requête de création d'une remorque. */
public record RemorqueRequest(
    @NotBlank String immatriculation,
    TypeRemorque type,
    @NotNull TypeCarrosserie carrosserie,
    String numeroParc,
    String vin,
    String marque,
    String modele,
    Integer anneeFabrication,
    Double poidsVideKg,
    @PositiveOrZero double volumeUtileM3,
    @PositiveOrZero int nbPositionsPalettes,
    @PositiveOrZero double chargeUtileKg,
    Double longueurM,
    Double largeurM,
    Double hauteurM,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax,
    LocalDate datePremiereMiseCirculation,
    LocalDate dateAcquisition,
    LocalDate dateMiseEnService) {}
