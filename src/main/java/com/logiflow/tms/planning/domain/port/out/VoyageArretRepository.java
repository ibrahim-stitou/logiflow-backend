package com.logiflow.tms.planning.domain.port.out;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des arrêts ordonnés d'un voyage. */
public interface VoyageArretRepository {

  ArretVoyage sauvegarder(ArretVoyage arret);

  List<ArretVoyage> sauvegarderTous(List<ArretVoyage> arrets);

  Optional<ArretVoyage> parId(UUID id);

  List<ArretVoyage> parVoyageIdOrdonnes(UUID voyageId);

  void supprimerParVoyageId(UUID voyageId);

  /**
   * Insère un arrêt juste après {@code apresIndiceArret} et décale d'un rang les arrêts suivants.
   */
  ArretVoyage insererApresIndice(UUID voyageId, ArretVoyage nouvelArret, int apresIndiceArret);

  long compterParVoyageId(UUID voyageId);

  void supprimerParId(UUID arretId);

  /** Renumérote les arrêts restants de {@code 0} à {@code n-1} après une suppression. */
  void reindexerSequences(UUID voyageId);
}
