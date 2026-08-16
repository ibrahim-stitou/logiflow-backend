package com.logiflow.tms.fleet.application.command;

import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.domain.vo.DocumentVehicule;
import java.util.List;

/** Commande applicative de création d'un véhicule. */
public record CreerVehiculeCommand(
    String immatriculation,
    TypeVehicule type,
    double ptacKg,
    double chargeUtileKg,
    List<DocumentVehicule> documents) {}
