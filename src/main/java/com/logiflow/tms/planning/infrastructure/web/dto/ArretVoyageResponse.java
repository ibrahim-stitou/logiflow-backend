package com.logiflow.tms.planning.infrastructure.web.dto;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import java.util.UUID;

/** Arrêt ordonné d'un voyage, localisé (carte et frise de l'itinéraire). */
public record ArretVoyageResponse(
    UUID id,
    int indiceSequence,
    String libelle,
    UUID siteId,
    double latitude,
    double longitude,
    boolean estOriginal) {

  public static ArretVoyageResponse depuis(ArretVoyage arret) {
    return new ArretVoyageResponse(
        arret.id(),
        arret.indiceSequence(),
        arret.libelle(),
        arret.siteId(),
        arret.localisation().latitude(),
        arret.localisation().longitude(),
        arret.estOriginal());
  }
}
