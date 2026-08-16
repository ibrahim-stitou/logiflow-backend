package com.logiflow.tms.fleet.application.command;

import com.logiflow.tms.fleet.domain.vo.DocumentVehicule;
import java.util.List;

/** Commande applicative de mise à jour des documents d'un véhicule existant. */
public record MajVehiculeCommand(List<DocumentVehicule> documents) {}
