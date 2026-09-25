package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.Prestataire;
import com.logiflow.tms.maintenance.domain.model.TypePrestataire;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des prestataires. */
public interface PrestataireRepository {

  Prestataire sauvegarder(Prestataire prestataire);

  Optional<Prestataire> parId(UUID id);

  boolean existeParCode(String code);

  /** Filtres facultatifs ; tri par raison sociale. */
  Page<Prestataire> rechercher(
      String texte, TypePrestataire type, Boolean actif, PageRequest pageRequest);
}
