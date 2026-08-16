package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanEntretienRepository {

  PlanEntretien sauvegarder(PlanEntretien plan);

  Optional<PlanEntretien> parId(UUID id);

  List<PlanEntretien> parVehiculeId(UUID vehiculeId);
}
