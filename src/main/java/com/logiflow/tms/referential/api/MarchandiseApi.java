package com.logiflow.tms.referential.api;

import com.logiflow.tms.referential.api.dto.MarchandiseSummary;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrat public du sous-domaine Marchandise, seul point d'entrée autorisé pour les autres modules
 * (ex. {@code order} et {@code dossier} pour valider les lignes de marchandise).
 */
public interface MarchandiseApi {

  /** Consulte la vue publique d'une marchandise, vide si elle n'existe pas. */
  Optional<MarchandiseSummary> consulter(UUID marchandiseId);

  /** Indique si la marchandise existe et est active. */
  boolean estActif(UUID marchandiseId);

  /**
   * Indique si la marchandise est classée matière dangereuse (classe ADR renseignée), utilisé par
   * défaut par les lignes qui ne surchargent pas cette information.
   */
  boolean estDangereuse(UUID marchandiseId);
}
