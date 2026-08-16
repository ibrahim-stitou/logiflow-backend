package com.logiflow.tms.fleet.infrastructure.persistence.mapper;

import com.logiflow.tms.fleet.domain.model.Remorque;
import com.logiflow.tms.fleet.domain.model.StatutVehicule;
import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.infrastructure.persistence.entity.RemorqueEntity;
import com.logiflow.tms.shared.domain.vo.Capacite;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import java.util.UUID;
import org.mapstruct.Mapper;

/** Traduit entre le modèle de domaine {@link Remorque} et l'entité JPA {@link RemorqueEntity}. */
@Mapper(componentModel = "spring")
public interface RemorqueMapper {

  UUID TENANT_PAR_DEFAUT = UUID.fromString("00000000-0000-0000-0000-000000000000");

  default Remorque versDomaine(RemorqueEntity entity) {
    if (entity == null) {
      return null;
    }
    return Remorque.reconstituer(
        entity.getId(),
        new Immatriculation(entity.getImmatriculation()),
        TypeCarrosserie.valueOf(entity.getCarrosserie()),
        new Capacite(
            (int) Math.round(entity.getChargeUtileKg()),
            entity.getVolumeUtileM3(),
            entity.getNbPositionsPalettes()),
        entity.isGroupeFroid(),
        entity.getTemperatureMin(),
        entity.getTemperatureMax(),
        StatutVehicule.valueOf(entity.getStatut()));
  }

  default RemorqueEntity versEntite(Remorque remorque) {
    if (remorque == null) {
      return null;
    }
    Capacite capacite = remorque.capaciteUtile();
    return RemorqueEntity.builder()
        .id(remorque.id())
        .tenantId(TENANT_PAR_DEFAUT)
        .immatriculation(remorque.immatriculation().valeur())
        .carrosserie(remorque.carrosserie().name())
        .volumeUtileM3(capacite.volumeM3())
        .nbPositionsPalettes(capacite.positionsPalettes())
        .chargeUtileKg(capacite.poidsKg())
        .groupeFroid(remorque.groupeFroid())
        .temperatureMin(remorque.temperatureMin())
        .temperatureMax(remorque.temperatureMax())
        .statut(remorque.statut().name())
        .build();
  }
}
