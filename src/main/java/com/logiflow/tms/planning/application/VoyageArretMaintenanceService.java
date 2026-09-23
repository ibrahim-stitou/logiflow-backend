package com.logiflow.tms.planning.application;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Maintenance des arrêts voyage (purge des points insérés devenus orphelins). */
@Service
@RequiredArgsConstructor
public class VoyageArretMaintenanceService {

  private final VoyageArretRepository voyageArretRepository;
  private final DossierApi dossierApi;

  /**
   * Supprime les arrêts non originaux qui ne sont plus référencés par aucun dossier du voyage, puis
   * renumérote les indices restants.
   */
  @Transactional
  public int purgerArretsOrphelins(UUID voyageId, List<UUID> dossierIds) {
    Set<UUID> arretsReferences = dossierApi.listerArretsVoyageReferences(dossierIds);
    List<ArretVoyage> arrets = voyageArretRepository.parVoyageIdOrdonnes(voyageId);
    List<UUID> aSupprimer =
        arrets.stream()
            .filter(arret -> !arret.estOriginal())
            .filter(arret -> !arretsReferences.contains(arret.id()))
            .map(ArretVoyage::id)
            .toList();

    for (UUID arretId : aSupprimer) {
      voyageArretRepository.supprimerParId(arretId);
    }
    if (!aSupprimer.isEmpty()) {
      voyageArretRepository.reindexerSequences(voyageId);
    }
    return aSupprimer.size();
  }
}
