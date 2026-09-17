package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdreTravailRepository {

  OrdreTravail sauvegarder(OrdreTravail ordreTravail);

  Optional<OrdreTravail> parId(UUID id);

  List<OrdreTravail> parVehiculeId(UUID vehiculeId);

  Page<OrdreTravail> rechercher(UUID vehiculeId, PageRequest pageRequest);
}
