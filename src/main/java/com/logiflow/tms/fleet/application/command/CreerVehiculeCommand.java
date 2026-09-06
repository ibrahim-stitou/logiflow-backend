package com.logiflow.tms.fleet.application.command;

import com.logiflow.tms.fleet.domain.model.Energie;
import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import java.time.LocalDate;

/** Commande applicative de création d'un véhicule. */
public record CreerVehiculeCommand(
    String immatriculation,
    TypeVehicule type,
    String numeroParc,
    String vin,
    String marque,
    String modele,
    Integer anneeMiseEnCirculation,
    Energie energie,
    double ptacKg,
    Double poidsVideKg,
    double chargeUtileKg,
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
