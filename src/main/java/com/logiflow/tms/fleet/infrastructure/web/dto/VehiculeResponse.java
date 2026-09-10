package com.logiflow.tms.fleet.infrastructure.web.dto;

import com.logiflow.tms.fleet.domain.model.Vehicule;
import java.time.LocalDate;
import java.util.UUID;

public record VehiculeResponse(
    UUID id,
    String immatriculation,
    String type,
    String numeroParc,
    String vin,
    String marque,
    String modele,
    Integer anneeMiseEnCirculation,
    String energie,
    double ptacKg,
    Double poidsVideKg,
    double chargeUtileKg,
    Double longueurM,
    Double largeurM,
    Double hauteurM,
    Double volumeUtileM3,
    Integer nbPositionsPalettes,
    String typeCarrosserie,
    boolean groupeFroid,
    Double temperatureMin,
    Double temperatureMax,
    int kilometrage,
    int heuresMoteur,
    String statut,
    LocalDate datePremiereMiseCirculation,
    LocalDate dateAcquisition,
    LocalDate dateMiseEnService,
    LocalDate dateSortie,
    String motifSortie,
    Integer kilometrageSortie,
    Integer heuresMoteurSortie) {

  public static VehiculeResponse depuis(Vehicule vehicule) {
    return new VehiculeResponse(
        vehicule.id(),
        vehicule.immatriculation().valeur(),
        vehicule.type().name(),
        vehicule.numeroParc(),
        vehicule.vin(),
        vehicule.marque(),
        vehicule.modele(),
        vehicule.anneeMiseEnCirculation(),
        vehicule.energie() != null ? vehicule.energie().name() : null,
        vehicule.ptac().kg(),
        vehicule.poidsVide() != null ? vehicule.poidsVide().kg() : null,
        vehicule.chargeUtile().kg(),
        vehicule.longueurM(),
        vehicule.largeurM(),
        vehicule.hauteurM(),
        vehicule.volumeUtileM3(),
        vehicule.nbPositionsPalettes(),
        vehicule.typeCarrosserie() != null ? vehicule.typeCarrosserie().name() : null,
        vehicule.groupeFroid(),
        vehicule.temperatureMin(),
        vehicule.temperatureMax(),
        vehicule.kilometrage(),
        vehicule.heuresMoteur(),
        vehicule.statut().name(),
        vehicule.datePremiereMiseCirculation(),
        vehicule.dateAcquisition(),
        vehicule.dateMiseEnService(),
        vehicule.dateSortie(),
        vehicule.motifSortie(),
        vehicule.kilometrageSortie(),
        vehicule.heuresMoteurSortie());
  }
}
