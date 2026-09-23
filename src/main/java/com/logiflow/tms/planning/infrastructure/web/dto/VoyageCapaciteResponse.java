package com.logiflow.tms.planning.infrastructure.web.dto;

import com.logiflow.tms.planning.application.VoyageCapaciteService.CapaciteVoyageVue;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.service.CapaciteTronconDomainService.UtilisationTroncon;
import com.logiflow.tms.shared.domain.vo.Capacite;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Utilisation de capacité remorque par tronçon d'itinéraire pour un voyage. */
public record VoyageCapaciteResponse(
    double capacitePoidsKg,
    double capaciteVolumeM3,
    List<ArretCapaciteResponse> arrets,
    List<TronconCapaciteResponse> troncons) {

  public record ArretCapaciteResponse(UUID id, int indiceSequence, String libelle) {}

  public record TronconCapaciteResponse(
      int indiceTroncon,
      UUID arretDepartId,
      UUID arretArriveeId,
      double poidsUtiliseKg,
      double volumeUtiliseM3,
      double pourcentagePoids,
      double pourcentageVolume,
      double pourcentageMax) {}

  public static VoyageCapaciteResponse depuis(CapaciteVoyageVue vue) {
    Capacite capacite = vue.capaciteRemorque();
    List<ArretVoyage> arretsOrdonnes =
        vue.arrets().stream().sorted(Comparator.comparingInt(ArretVoyage::indiceSequence)).toList();
    List<ArretCapaciteResponse> arrets =
        arretsOrdonnes.stream()
            .map(
                arret ->
                    new ArretCapaciteResponse(
                        arret.id(), arret.indiceSequence(), arret.libelle()))
            .toList();

    List<TronconCapaciteResponse> troncons =
        vue.troncons().stream()
            .map(
                troncon ->
                    versTroncon(troncon, capacite.poidsKg(), capacite.volumeM3()))
            .toList();

    return new VoyageCapaciteResponse(
        capacite.poidsKg(), capacite.volumeM3(), arrets, troncons);
  }

  private static TronconCapaciteResponse versTroncon(
      UtilisationTroncon troncon, int capacitePoidsKg, double capaciteVolumeM3) {
    double pourcentagePoids =
        capacitePoidsKg > 0 ? (troncon.poidsUtiliseKg() / capacitePoidsKg) * 100d : 0d;
    double pourcentageVolume =
        capaciteVolumeM3 > 0 ? (troncon.volumeUtiliseM3() / capaciteVolumeM3) * 100d : 0d;
    return new TronconCapaciteResponse(
        troncon.indiceTroncon(),
        troncon.arretDepartId(),
        troncon.arretArriveeId(),
        troncon.poidsUtiliseKg(),
        troncon.volumeUtiliseM3(),
        pourcentagePoids,
        pourcentageVolume,
        Math.max(pourcentagePoids, pourcentageVolume));
  }
}
