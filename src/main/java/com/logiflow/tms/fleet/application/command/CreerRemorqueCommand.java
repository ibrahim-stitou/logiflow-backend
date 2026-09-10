package com.logiflow.tms.fleet.application.command;

import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.domain.model.TypeRemorque;
import java.time.LocalDate;

/** Commande applicative de création d'une remorque. */
public record CreerRemorqueCommand(
    String immatriculation,
    TypeRemorque type,
    TypeCarrosserie carrosserie,
    String numeroParc,
    String vin,
    String marque,
    String modele,
    Integer anneeFabrication,
    Double poidsVideKg,
    double volumeUtileM3,
    int nbPositionsPalettes,
    double chargeUtileKg,
    Double longueurM,
    Double largeurM,
    Double hauteurM,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax,
    LocalDate datePremiereMiseCirculation,
    LocalDate dateAcquisition,
    LocalDate dateMiseEnService) {}
