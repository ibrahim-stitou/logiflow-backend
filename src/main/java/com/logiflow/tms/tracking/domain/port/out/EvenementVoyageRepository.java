package com.logiflow.tms.tracking.domain.port.out;

import com.logiflow.tms.tracking.domain.model.EvenementVoyage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des événements de voyage (journal append-only). */
public interface EvenementVoyageRepository {

  EvenementVoyage sauvegarder(EvenementVoyage evenement);

  Optional<EvenementVoyage> parId(UUID id);

  /** Événements d'un voyage, ordonnés du plus ancien au plus récent. */
  List<EvenementVoyage> parVoyageId(UUID voyageId);

  Optional<EvenementVoyage> dernierParVoyageId(UUID voyageId);
}
