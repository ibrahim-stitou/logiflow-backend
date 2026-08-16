package com.logiflow.tms.planning.infrastructure.web.dto;

import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Trajet;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VoyageResponse(
    UUID id,
    String reference,
    String statut,
    TypeVoyage typeVoyage,
    Portee portee,
    Instant departPrevu,
    Instant arriveePrevue,
    UUID vehiculeId,
    UUID remorqueId,
    List<UUID> dossierIds,
    Trajet trajet,
    List<Affectation> affectations,
    double tauxRemplissage) {

  public static VoyageResponse depuis(Voyage voyage) {
    return new VoyageResponse(
        voyage.id(),
        voyage.reference().valeur(),
        voyage.statut().name(),
        voyage.typeVoyage(),
        voyage.portee(),
        voyage.departPrevu(),
        voyage.arriveePrevue(),
        voyage.vehiculeId(),
        voyage.remorqueId(),
        voyage.dossierIds(),
        voyage.trajet(),
        voyage.affectations(),
        voyage.tauxRemplissage());
  }
}
