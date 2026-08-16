package com.logiflow.tms.planning.domain.vo;

import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Affectation d'un chauffeur à un voyage. */
public record Affectation(UUID chauffeurId, RoleChauffeur role, Instant dateAffectation) {

  public Affectation {
    Objects.requireNonNull(chauffeurId, "Le chauffeur est obligatoire");
    Objects.requireNonNull(role, "Le rôle du chauffeur est obligatoire");
    Objects.requireNonNull(dateAffectation, "La date d'affectation est obligatoire");
  }
}
