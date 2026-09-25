package com.logiflow.tms.fleet.infrastructure.persistence.mapper;

import com.logiflow.tms.fleet.domain.model.Energie;
import com.logiflow.tms.fleet.domain.model.StatutVehicule;
import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.fleet.infrastructure.persistence.entity.VehiculeEntity;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import org.mapstruct.Mapper;

/** Traduit entre le modèle de domaine {@link Vehicule} et l'entité JPA {@link VehiculeEntity}. */
@Mapper(componentModel = "spring")
public interface VehiculeMapper {

  default Vehicule versDomaine(VehiculeEntity entity) {
    if (entity == null) {
      return null;
    }
    return Vehicule.reconstituer(
        entity.getId(),
        new Immatriculation(entity.getImmatriculation()),
        TypeVehicule.valueOf(entity.getType()),
        entity.getNumeroParc(),
        entity.getVin(),
        entity.getMarque(),
        entity.getModele(),
        entity.getAnneeMiseEnCirculation(),
        entity.getEnergie() != null ? Energie.valueOf(entity.getEnergie()) : null,
        new Poids(entity.getPtacKg()),
        entity.getPoidsVideKg() != null ? new Poids(entity.getPoidsVideKg()) : null,
        new Poids(entity.getChargeUtileKg()),
        entity.getLongueurM(),
        entity.getLargeurM(),
        entity.getHauteurM(),
        entity.getVolumeUtileM3(),
        entity.getNbPositionsPalettes(),
        entity.getTypeCarrosserie() != null
            ? TypeCarrosserie.valueOf(entity.getTypeCarrosserie())
            : null,
        entity.isGroupeFroid(),
        entity.getTemperatureMin(),
        entity.getTemperatureMax(),
        entity.getKilometrage(),
        entity.getHeuresMoteur(),
        StatutVehicule.valueOf(entity.getStatut()),
        entity.getDatePremiereMiseCirculation(),
        entity.getDateAcquisition(),
        entity.getDateMiseEnService(),
        entity.getDateSortie(),
        entity.getMotifSortie(),
        entity.getKilometrageSortie(),
        entity.getHeuresMoteurSortie());
  }

  default VehiculeEntity versEntite(Vehicule vehicule) {
    if (vehicule == null) {
      return null;
    }
    return VehiculeEntity.builder()
        .id(vehicule.id())
        .immatriculation(vehicule.immatriculation().valeur())
        .type(vehicule.type().name())
        .numeroParc(vehicule.numeroParc())
        .vin(vehicule.vin())
        .marque(vehicule.marque())
        .modele(vehicule.modele())
        .anneeMiseEnCirculation(vehicule.anneeMiseEnCirculation())
        .energie(vehicule.energie() != null ? vehicule.energie().name() : null)
        .ptacKg(vehicule.ptac().kg())
        .poidsVideKg(vehicule.poidsVide() != null ? vehicule.poidsVide().kg() : null)
        .chargeUtileKg(vehicule.chargeUtile().kg())
        .longueurM(vehicule.longueurM())
        .largeurM(vehicule.largeurM())
        .hauteurM(vehicule.hauteurM())
        .volumeUtileM3(vehicule.volumeUtileM3())
        .nbPositionsPalettes(vehicule.nbPositionsPalettes())
        .typeCarrosserie(
            vehicule.typeCarrosserie() != null ? vehicule.typeCarrosserie().name() : null)
        .groupeFroid(vehicule.groupeFroid())
        .temperatureMin(vehicule.temperatureMin())
        .temperatureMax(vehicule.temperatureMax())
        .kilometrage(vehicule.kilometrage())
        .heuresMoteur(vehicule.heuresMoteur())
        .statut(vehicule.statut().name())
        .datePremiereMiseCirculation(vehicule.datePremiereMiseCirculation())
        .dateAcquisition(vehicule.dateAcquisition())
        .dateMiseEnService(vehicule.dateMiseEnService())
        .dateSortie(vehicule.dateSortie())
        .motifSortie(vehicule.motifSortie())
        .kilometrageSortie(vehicule.kilometrageSortie())
        .heuresMoteurSortie(vehicule.heuresMoteurSortie())
        .build();
  }

  /** Met à jour une entité gérée à partir du domaine, sans remplacer l'identité JPA. */
  default void mettreAJour(VehiculeEntity entity, Vehicule vehicule) {
    if (entity == null || vehicule == null) {
      return;
    }
    entity.ecraserEtatMetier(
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
