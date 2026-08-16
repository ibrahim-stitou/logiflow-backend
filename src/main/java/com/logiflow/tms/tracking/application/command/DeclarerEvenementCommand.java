package com.logiflow.tms.tracking.application.command;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.tracking.domain.model.TypeEvenement;
import java.time.Instant;
import java.util.UUID;

/** Commande applicative de déclaration d'un événement d'exécution de voyage. */
public record DeclarerEvenementCommand(
    UUID voyageId, TypeEvenement type, Instant horodatage, GeoPoint position, String commentaire) {}
