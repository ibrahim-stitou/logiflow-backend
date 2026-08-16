package com.logiflow.tms.fleet.application.command;

import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;

/** Commande applicative de création d'une remorque. */
public record CreerRemorqueCommand(
    String immatriculation,
    TypeCarrosserie carrosserie,
    double volumeUtileM3,
    int nbPositionsPalettes,
    double chargeUtileKg,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax) {}
