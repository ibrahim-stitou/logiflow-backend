package com.logiflow.tms.dossier.domain.port.out;

import com.logiflow.tms.dossier.domain.model.DossierTransport;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des dossiers de transport. */
public interface DossierTransportRepository {

  DossierTransport sauvegarder(DossierTransport dossier);

  Optional<DossierTransport> parId(UUID id);

  Optional<DossierTransport> parReference(String reference);

  List<DossierTransport> parCommandeId(UUID commandeId);

  Page<DossierTransport> rechercher(String texteRecherche, PageRequest pageRequest);

  /**
   * Recherche filtrée par statut (optionnel, {@code null} = tous) et texte (optionnel), triée de la
   * plus récemment modifiée à la plus ancienne.
   */
  Page<DossierTransport> rechercherParStatut(
      String texteRecherche, String statut, PageRequest pageRequest);
}
