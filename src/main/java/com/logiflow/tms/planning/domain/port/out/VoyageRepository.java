package com.logiflow.tms.planning.domain.port.out;

import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des voyages. */
public interface VoyageRepository {

  Voyage sauvegarder(Voyage voyage);

  Optional<Voyage> parId(UUID id);

  Optional<Voyage> parReference(String reference);

  Page<Voyage> rechercher(PageRequest pageRequest);

  List<Voyage> parDossierId(UUID dossierId);
}
