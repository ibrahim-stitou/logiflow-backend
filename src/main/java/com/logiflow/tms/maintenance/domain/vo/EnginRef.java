package com.logiflow.tms.maintenance.domain.vo;

import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import java.util.Objects;
import java.util.UUID;

/** Référence vers un engin du module {@code fleet} : véhicule ou remorque. */
public record EnginRef(TypeEngin type, UUID id) {

  public EnginRef {
    Objects.requireNonNull(type, "Le type d'engin est obligatoire");
    Objects.requireNonNull(id, "L'identifiant de l'engin est obligatoire");
  }

  public static EnginRef vehicule(UUID id) {
    return new EnginRef(TypeEngin.VEHICULE, id);
  }

  public static EnginRef remorque(UUID id) {
    return new EnginRef(TypeEngin.REMORQUE, id);
  }
}
