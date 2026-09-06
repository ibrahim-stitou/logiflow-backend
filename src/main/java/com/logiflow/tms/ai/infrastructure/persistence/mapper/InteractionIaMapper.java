package com.logiflow.tms.ai.infrastructure.persistence.mapper;

import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.TypeInteractionIa;
import com.logiflow.tms.ai.infrastructure.persistence.entity.InteractionIaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InteractionIaMapper {

  default InteractionIa versDomaine(InteractionIaEntity entity) {
    if (entity == null) {
      return null;
    }
    return InteractionIa.reconstituer(
        entity.getId(),
        TypeInteractionIa.valueOf(entity.getTypeInteraction()),
        entity.getCreatedAt(),
        entity.getUtilisateurId(),
        entity.isSucces(),
        entity.getDureeMs(),
        entity.getResume(),
        entity.getErreur());
  }

  default InteractionIaEntity versEntite(InteractionIa interaction) {
    if (interaction == null) {
      return null;
    }
    return InteractionIaEntity.builder()
        .id(interaction.id())
        .typeInteraction(interaction.type().name())
        .utilisateurId(interaction.utilisateurId())
        .succes(interaction.succes())
        .dureeMs(interaction.dureeMs())
        .resume(interaction.resume())
        .erreur(interaction.erreur())
        .build();
  }
}
