package com.logiflow.tms.fleet.infrastructure.persistence.mapper;

import com.logiflow.tms.fleet.domain.model.Remorque;
import com.logiflow.tms.fleet.domain.model.StatutVehicule;
import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.domain.model.TypeRemorque;
import com.logiflow.tms.fleet.infrastructure.persistence.entity.RemorqueEntity;
import com.logiflow.tms.shared.domain.vo.Capacite;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import org.mapstruct.Mapper;

/** Traduit entre le modèle de domaine {@link Remorque} et l'entité JPA {@link RemorqueEntity}. */
@Mapper(componentModel = "spring")
public interface RemorqueMapper {

  default Remorque versDomaine(RemorqueEntity entity) {
    if (entity == null) {
      return null;
    }
    return Remorque.reconstituer(
        entity.getId(),
        new Immatriculation(entity.getImmatriculation()),
        entity.getType() != null ? TypeRemorque.valueOf(entity.getType()) : null,
        TypeCarrosserie.valueOf(entity.getCarrosserie()),
        entity.getNumeroParc(),
        entity.getVin(),
        entity.getMarque(),
        entity.getModele(),
        entity.getAnneeFabrication(),
        entity.getPoidsVideKg() != null ? new Poids(entity.getPoidsVideKg()) : null,
        new Capacite(
            (int) Math.round(entity.getChargeUtileKg()),
            entity.getVolumeUtileM3(),
            entity.getNbPositionsPalettes()),
        entity.getLongueurM(),
        entity.getLargeurM(),
        entity.getHauteurM(),
        entity.isGroupeFroid(),
        entity.getTemperatureMin(),
        entity.getTemperatureMax(),
        entity.getKilometrage(),
        entity.getHeuresGroupeFroid(),
        StatutVehicule.valueOf(entity.getStatut()),
        entity.getDatePremiereMiseCirculation(),
        entity.getDateAcquisition(),
        entity.getDateMiseEnService(),
        entity.getDateSortie(),
        entity.getMotifSortie(),
        entity.getKilometrageSortie(),
        entity.getHeuresGroupeFroidSortie());
  }

  default RemorqueEntity versEntite(Remorque remorque) {
    if (remorque == null) {
      return null;
    }
    Capacite capacite = remorque.capaciteUtile();
    return RemorqueEntity.builder()
        .id(remorque.id())
        .immatriculation(remorque.immatriculation().valeur())
        .type(remorque.type() != null ? remorque.type().name() : null)
        .carrosserie(remorque.carrosserie().name())
        .numeroParc(remorque.numeroParc())
        .vin(remorque.vin())
        .marque(remorque.marque())
        .modele(remorque.modele())
        .anneeFabrication(remorque.anneeFabrication())
        .poidsVideKg(remorque.poidsVide() != null ? remorque.poidsVide().kg() : null)
        .volumeUtileM3(capacite.volumeM3())
        .nbPositionsPalettes(capacite.positionsPalettes())
        .chargeUtileKg(capacite.poidsKg())
        .longueurM(remorque.longueurM())
        .largeurM(remorque.largeurM())
        .hauteurM(remorque.hauteurM())
        .groupeFroid(remorque.groupeFroid())
        .temperatureMin(remorque.temperatureMin())
        .temperatureMax(remorque.temperatureMax())
        .kilometrage(remorque.kilometrage())
        .heuresGroupeFroid(remorque.heuresGroupeFroid())
        .statut(remorque.statut().name())
        .datePremiereMiseCirculation(remorque.datePremiereMiseCirculation())
        .dateAcquisition(remorque.dateAcquisition())
        .dateMiseEnService(remorque.dateMiseEnService())
        .dateSortie(remorque.dateSortie())
        .motifSortie(remorque.motifSortie())
        .kilometrageSortie(remorque.kilometrageSortie())
        .heuresGroupeFroidSortie(remorque.heuresGroupeFroidSortie())
        .build();
  }
}
