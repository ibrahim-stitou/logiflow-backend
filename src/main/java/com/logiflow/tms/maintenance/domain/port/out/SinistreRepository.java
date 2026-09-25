package com.logiflow.tms.maintenance.domain.port.out;

import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.model.StatutSinistre;
import com.logiflow.tms.maintenance.domain.model.TypeSinistre;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des sinistres. */
public interface SinistreRepository {

  /** Critères de recherche, tous facultatifs. {@code enginId} : véhicule ou remorque. */
  record Filtre(
      UUID enginId,
      UUID chauffeurId,
      StatutSinistre statut,
      TypeSinistre type,
      String texte,
      LocalDateTime debut,
      LocalDateTime fin) {

    public static Filtre aucun() {
      return new Filtre(null, null, null, null, null, null, null);
    }
  }

  Sinistre sauvegarder(Sinistre sinistre);

  Optional<Sinistre> parId(UUID id);

  /** Recherche paginée, du plus récent au plus ancien. */
  Page<Sinistre> rechercher(Filtre filtre, PageRequest pageRequest);

  List<Sinistre> lister(Filtre filtre);
}
