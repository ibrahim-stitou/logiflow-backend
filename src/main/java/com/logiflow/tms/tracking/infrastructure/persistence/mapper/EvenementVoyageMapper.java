package com.logiflow.tms.tracking.infrastructure.persistence.mapper;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.tracking.domain.model.EvenementVoyage;
import com.logiflow.tms.tracking.domain.model.TypeEvenement;
import com.logiflow.tms.tracking.infrastructure.persistence.entity.EvenementVoyageEntity;
import java.util.UUID;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EvenementVoyageMapper {

  UUID TENANT_PAR_DEFAUT = UUID.fromString("00000000-0000-0000-0000-000000000000");

  default EvenementVoyage versDomaine(EvenementVoyageEntity entity) {
    if (entity == null) {
      return null;
    }
    GeoPoint position =
        entity.getLatitude() != null && entity.getLongitude() != null
            ? new GeoPoint(entity.getLatitude(), entity.getLongitude())
            : null;
    return EvenementVoyage.reconstituer(
        entity.getId(),
        entity.getVoyageId(),
        TypeEvenement.valueOf(entity.getTypeEvenement()),
        entity.getHorodatage(),
        position,
        entity.getCommentaire());
  }

  default EvenementVoyageEntity versEntite(EvenementVoyage evenement) {
    if (evenement == null) {
      return null;
    }
    return EvenementVoyageEntity.builder()
        .id(evenement.id())
        .tenantId(TENANT_PAR_DEFAUT)
        .voyageId(evenement.voyageId())
        .typeEvenement(evenement.type().name())
        .horodatage(evenement.horodatage())
        .latitude(evenement.position() != null ? evenement.position().latitude() : null)
        .longitude(evenement.position() != null ? evenement.position().longitude() : null)
        .commentaire(evenement.commentaire())
        .build();
  }
}
