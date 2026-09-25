package com.logiflow.tms.referential.infrastructure.persistence.mapper;

import com.logiflow.tms.referential.domain.model.Marchandise;
import com.logiflow.tms.referential.infrastructure.persistence.entity.MarchandiseEntity;
import org.mapstruct.Mapper;

/**
 * Traduit entre le modèle de domaine {@link Marchandise} et l'entité JPA {@link MarchandiseEntity}.
 */
@Mapper(componentModel = "spring")
public interface MarchandiseMapper {

  default Marchandise versDomaine(MarchandiseEntity entity) {
    if (entity == null) {
      return null;
    }
    return Marchandise.reconstituer(
        entity.getId(),
        entity.getCode(),
        entity.getLibelle(),
        entity.getFamille(),
        entity.getClasseAdr(),
        entity.getNumeroOnu(),
        entity.isGerbable(),
        entity.isActif());
  }

  default MarchandiseEntity versEntite(Marchandise marchandise) {
    if (marchandise == null) {
      return null;
    }
    return MarchandiseEntity.builder()
        .id(marchandise.id())
        .code(marchandise.code())
        .libelle(marchandise.libelle())
        .famille(marchandise.famille())
        .classeAdr(marchandise.classeAdr())
        .numeroOnu(marchandise.numeroOnu())
        .gerbable(marchandise.gerbable())
        .actif(marchandise.estActif())
        .build();
  }
}
