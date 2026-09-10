package com.logiflow.tms.referential.domain.port.out;

import com.logiflow.tms.referential.domain.model.Marchandise;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance du catalogue marchandise. */
public interface MarchandiseRepository {

  Marchandise sauvegarder(Marchandise marchandise);

  Optional<Marchandise> parId(UUID id);

  Optional<Marchandise> parCode(String code);

  boolean existeParCode(String code);

  Page<Marchandise> rechercher(String texteRecherche, PageRequest pageRequest);
}
