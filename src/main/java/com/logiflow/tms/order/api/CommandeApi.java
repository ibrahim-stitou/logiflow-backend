package com.logiflow.tms.order.api;

import com.logiflow.tms.order.api.dto.CommandeSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Commande, seul point d'entrée autorisé pour les autres modules
 * (ex. {@code dossier} pour générer les dossiers de transport d'une commande confirmée).
 */
public interface CommandeApi {

  /** Consulte la vue publique d'une commande, vide si elle n'existe pas. */
  Optional<CommandeSummary> consulter(UUID commandeId);

  /** Indique si la commande existe et est au statut CONFIRMEE. */
  boolean estConfirmee(UUID commandeId);

  /**
   * Recherche paginée pour la consultation transverse (copilote IA). {@code statut} optionnel
   * ({@code null} = tous), doit appartenir à {@link #statutsConnus()} ; {@code texte} optionnel.
   */
  Page<CommandeSummary> rechercher(String texte, String statut, PageRequest pageRequest);

  /** Valeurs possibles du filtre de {@link #rechercher}. */
  List<String> statutsConnus();
}
