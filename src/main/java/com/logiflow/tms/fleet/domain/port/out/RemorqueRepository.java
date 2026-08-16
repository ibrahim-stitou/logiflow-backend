package com.logiflow.tms.fleet.domain.port.out;

import com.logiflow.tms.fleet.domain.model.Remorque;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des remorques. */
public interface RemorqueRepository {

  Remorque sauvegarder(Remorque remorque);

  Optional<Remorque> parId(UUID id);

  Optional<Remorque> parImmatriculation(String immatriculation);

  boolean existeParImmatriculation(String immatriculation);

  Page<Remorque> rechercher(PageRequest pageRequest);
}
