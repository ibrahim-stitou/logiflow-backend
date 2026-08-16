package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.ScoreSante;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoreSanteRepository {

  ScoreSante sauvegarder(ScoreSante scoreSante);

  Optional<ScoreSante> parId(UUID id);

  Optional<ScoreSante> dernierParVehiculeId(UUID vehiculeId);

  List<ScoreSante> parVehiculeId(UUID vehiculeId);
}
