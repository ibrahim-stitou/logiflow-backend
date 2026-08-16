package com.logiflow.tms.ai.application.command;

import com.logiflow.tms.ai.domain.model.PointItineraire;
import java.util.List;

/** Commande applicative de calcul d'itinéraire routier passant par une liste de points ordonnée. */
public record CalculerItineraireCommand(List<PointItineraire> points) {}
