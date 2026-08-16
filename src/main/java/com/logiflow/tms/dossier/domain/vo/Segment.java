package com.logiflow.tms.dossier.domain.vo;

import com.logiflow.tms.dossier.domain.model.TypeSegment;
import com.logiflow.tms.shared.domain.vo.TimeWindow;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Point d'arrêt requis par un dossier de transport (chargement ou déchargement), localisé sur un
 * site.
 */
public record Segment(
    TypeSegment type, int ordre, UUID siteId, TimeWindow fenetre, Instant realiseLe) {

  public Segment {
    Objects.requireNonNull(type, "Le type de segment est obligatoire");
    if (ordre < 0) {
      throw new IllegalArgumentException("L'ordre du segment ne peut pas être négatif");
    }
    Objects.requireNonNull(siteId, "Le site du segment est obligatoire");
    Objects.requireNonNull(fenetre, "La fenêtre horaire du segment est obligatoire");
  }

  public boolean estRealise() {
    return realiseLe != null;
  }
}
