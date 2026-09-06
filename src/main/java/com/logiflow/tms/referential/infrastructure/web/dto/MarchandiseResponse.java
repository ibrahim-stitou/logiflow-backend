package com.logiflow.tms.referential.infrastructure.web.dto;

import com.logiflow.tms.referential.domain.model.Marchandise;
import java.util.UUID;

public record MarchandiseResponse(
    UUID id,
    String code,
    String libelle,
    String famille,
    String classeAdr,
    String numeroOnu,
    boolean gerbable,
    boolean actif) {

  public static MarchandiseResponse depuis(Marchandise marchandise) {
    return new MarchandiseResponse(
        marchandise.id(),
        marchandise.code(),
        marchandise.libelle(),
        marchandise.famille(),
        marchandise.classeAdr(),
        marchandise.numeroOnu(),
        marchandise.gerbable(),
        marchandise.estActif());
  }
}
