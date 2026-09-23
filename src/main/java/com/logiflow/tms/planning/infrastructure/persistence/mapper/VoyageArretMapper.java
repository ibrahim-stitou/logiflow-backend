package com.logiflow.tms.planning.infrastructure.persistence.mapper;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.infrastructure.persistence.entity.VoyageArretEntity;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import org.springframework.stereotype.Component;

/** Traduit entre {@link ArretVoyage} et {@link VoyageArretEntity}. */
@Component
public class VoyageArretMapper {

  public ArretVoyage versDomaine(VoyageArretEntity entity) {
    if (entity == null) {
      return null;
    }
    return ArretVoyage.reconstituer(
        entity.getId(),
        entity.getVoyageId(),
        entity.getIndiceSequence(),
        entity.getLibelle(),
        new GeoPoint(entity.getLatitude(), entity.getLongitude()),
        entity.getSiteId(),
        entity.isEstOriginal());
  }

  public VoyageArretEntity versEntite(ArretVoyage arret) {
    if (arret == null) {
      return null;
    }
    return VoyageArretEntity.builder()
        .id(arret.id())
        .voyageId(arret.voyageId())
        .indiceSequence(arret.indiceSequence())
        .libelle(arret.libelle())
        .latitude(arret.localisation().latitude())
        .longitude(arret.localisation().longitude())
        .siteId(arret.siteId())
        .estOriginal(arret.estOriginal())
        .build();
  }
}
