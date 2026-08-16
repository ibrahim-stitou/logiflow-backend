package com.logiflow.tms.referential.domain.port.out;

import com.logiflow.tms.referential.domain.model.Site;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des sites, implémenté par l'adaptateur JPA. */
public interface SiteRepository {

  Site sauvegarder(Site site);

  Optional<Site> parId(UUID id);

  Optional<Site> parCode(String code);

  boolean existeParCode(String code);

  Page<Site> rechercher(String texteRecherche, PageRequest pageRequest);
}
