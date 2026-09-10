package com.logiflow.tms.maintenance.infrastructure.persistence.mapper;

import com.logiflow.tms.maintenance.domain.model.ScoreSante;
import com.logiflow.tms.maintenance.domain.model.StatutSante;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.ScoreSanteEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScoreSanteMapper {

  default ScoreSante versDomaine(ScoreSanteEntity entity) {
    if (entity == null) {
      return null;
    }
    return ScoreSante.reconstituer(
        entity.getId(),
        entity.getVehiculeId(),
        entity.getCalculeLe(),
        entity.getScore(),
        StatutSante.valueOf(entity.getStatut()),
        entity.getKmAvantEcheance(),
        entity.getDateEcheanceProjetee(),
        entity.getRecommandation());
  }

  default ScoreSanteEntity versEntite(ScoreSante scoreSante) {
    if (scoreSante == null) {
      return null;
    }
    return ScoreSanteEntity.builder()
        .id(scoreSante.id())
        .vehiculeId(scoreSante.vehiculeId())
        .calculeLe(scoreSante.calculeLe())
        .score(scoreSante.score())
        .statut(scoreSante.statut().name())
        .kmAvantEcheance(scoreSante.kmAvantEcheance())
        .dateEcheanceProjetee(scoreSante.dateEcheanceProjetee())
        .recommandation(scoreSante.recommandation())
        .build();
  }
}
