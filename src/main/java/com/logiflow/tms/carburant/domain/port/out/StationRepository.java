package com.logiflow.tms.carburant.domain.port.out;

import com.logiflow.tms.carburant.domain.model.Station;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

public interface StationRepository {

  Station sauvegarder(Station station);

  Optional<Station> parId(UUID id);

  boolean existeParCode(String code);

  Page<Station> rechercher(String texteRecherche, PageRequest pageRequest);
}
