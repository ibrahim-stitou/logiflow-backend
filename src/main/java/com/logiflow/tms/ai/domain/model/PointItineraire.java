package com.logiflow.tms.ai.domain.model;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.Objects;

/** Point de passage d'un itinéraire (position géographique + libellé optionnel). */
public record PointItineraire(GeoPoint position, String libelle) {

  public PointItineraire {
    Objects.requireNonNull(position, "La position géographique est obligatoire");
  }
}
