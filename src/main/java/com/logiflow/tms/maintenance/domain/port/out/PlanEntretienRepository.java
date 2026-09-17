package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanEntretienRepository {

  PlanEntretien sauvegarder(PlanEntretien plan);

  Optional<PlanEntretien> parId(UUID id);

  List<PlanEntretien> parVehiculeId(UUID vehiculeId);

  Page<PlanEntretien> rechercher(UUID vehiculeId, PageRequest pageRequest);
}
