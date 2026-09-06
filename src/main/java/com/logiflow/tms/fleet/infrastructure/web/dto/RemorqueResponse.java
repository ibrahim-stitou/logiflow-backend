package com.logiflow.tms.fleet.infrastructure.web.dto;

import com.logiflow.tms.fleet.domain.model.Remorque;
import java.time.LocalDate;
import java.util.UUID;

public record RemorqueResponse(
    UUID id,
    String immatriculation,
    String type,
    String carrosserie,
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
    int kilometrage,
    int heuresGroupeFroid,
    String statut,
    LocalDate datePremiereMiseCirculation,
    LocalDate dateAcquisition,
    LocalDate dateMiseEnService,
    LocalDate dateSortie,
    String motifSortie,
    Integer kilometrageSortie,
    Integer heuresGroupeFroidSortie) {

  public static RemorqueResponse depuis(Remorque remorque) {
    var capacite = remorque.capaciteUtile();
    return new RemorqueResponse(
        remorque.id(),
        remorque.immatriculation().valeur(),
        remorque.type() != null ? remorque.type().name() : null,
        remorque.carrosserie().name(),
        remorque.numeroParc(),
        remorque.vin(),
        remorque.marque(),
        remorque.modele(),
        remorque.anneeFabrication(),
        remorque.poidsVide() != null ? remorque.poidsVide().kg() : null,
        capacite.volumeM3(),
        capacite.positionsPalettes(),
        capacite.poidsKg(),
        remorque.longueurM(),
        remorque.largeurM(),
        remorque.hauteurM(),
        remorque.groupeFroid(),
        remorque.temperatureMin(),
        remorque.temperatureMax(),
        remorque.kilometrage(),
        remorque.heuresGroupeFroid(),
        remorque.statut().name(),
        remorque.datePremiereMiseCirculation(),
        remorque.dateAcquisition(),
        remorque.dateMiseEnService(),
        remorque.dateSortie(),
        remorque.motifSortie(),
        remorque.kilometrageSortie(),
        remorque.heuresGroupeFroidSortie());
  }
}
