package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.ContratAssurance;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des contrats d'assurance. */
public interface ContratAssuranceRepository {

  ContratAssurance sauvegarder(ContratAssurance contrat);

  Optional<ContratAssurance> parId(UUID id);

  /** Tri par échéance la plus proche. */
  Page<ContratAssurance> rechercher(Boolean actif, PageRequest pageRequest);

  List<ContratAssurance> actifs();
}
