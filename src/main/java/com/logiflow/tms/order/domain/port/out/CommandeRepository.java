package com.logiflow.tms.order.domain.port.out;

import com.logiflow.tms.order.domain.model.Commande;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des commandes. */
public interface CommandeRepository {

  Commande sauvegarder(Commande commande);

  Optional<Commande> parId(UUID id);

  Optional<Commande> parReference(String reference);

  Page<Commande> rechercher(String texteRecherche, PageRequest pageRequest);

  /**
   * Recherche filtrée par statut (optionnel, {@code null} = tous) et texte (optionnel), triée de la
   * plus récemment modifiée à la plus ancienne.
   */
  Page<Commande> rechercherParStatut(String texteRecherche, String statut, PageRequest pageRequest);
}
