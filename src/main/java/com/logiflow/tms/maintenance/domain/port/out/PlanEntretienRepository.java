package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des plans d'entretien. */
public interface PlanEntretienRepository {

  PlanEntretien sauvegarder(PlanEntretien plan);

  Optional<PlanEntretien> parId(UUID id);

  /** Filtres facultatifs (null = tous). */
  Page<PlanEntretien> rechercher(
      TypeEngin typeEngin, UUID enginId, Boolean actif, PageRequest pageRequest);

  /** Plans actifs de toute la flotte (calcul des échéances). */
  List<PlanEntretien> actifs();
}
