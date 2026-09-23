package com.logiflow.tms.planning.infrastructure.persistence.adapter;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.infrastructure.persistence.mapper.VoyageArretMapper;
import com.logiflow.tms.planning.infrastructure.persistence.repository.VoyageArretJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class VoyageArretRepositoryAdapter implements VoyageArretRepository {

  private final VoyageArretJpaRepository jpaRepository;
  private final VoyageArretMapper mapper;

  @Override
  public ArretVoyage sauvegarder(ArretVoyage arret) {
    var entite = jpaRepository.save(mapper.versEntite(arret));
    return mapper.versDomaine(entite);
  }

  @Override
  public List<ArretVoyage> sauvegarderTous(List<ArretVoyage> arrets) {
    return jpaRepository
        .saveAll(arrets.stream().map(mapper::versEntite).toList())
        .stream()
        .map(mapper::versDomaine)
        .toList();
  }

  @Override
  public Optional<ArretVoyage> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public List<ArretVoyage> parVoyageIdOrdonnes(UUID voyageId) {
    return jpaRepository.findByVoyageIdOrderByIndiceSequenceAsc(voyageId).stream()
        .map(mapper::versDomaine)
        .toList();
  }

  @Override
  @Transactional
  public void supprimerParVoyageId(UUID voyageId) {
    jpaRepository.deleteByVoyageId(voyageId);
  }

  @Override
  @Transactional
  public ArretVoyage insererApresIndice(UUID voyageId, ArretVoyage nouvelArret, int apresIndiceArret) {
    var entites = jpaRepository.findByVoyageIdOrderByIndiceSequenceAsc(voyageId);
    // Décaler via indices temporaires pour éviter la contrainte unique (voyage_id, indice_sequence).
    for (var entite : entites) {
      if (entite.getIndiceSequence() > apresIndiceArret) {
        entite.mettreAJourIndiceSequence(entite.getIndiceSequence() + 10_000);
      }
    }
    jpaRepository.saveAll(entites);
    jpaRepository.flush();
    for (var entite : entites) {
      if (entite.getIndiceSequence() > 10_000) {
        entite.mettreAJourIndiceSequence(entite.getIndiceSequence() - 10_000 + 1);
      }
    }
    jpaRepository.saveAll(entites);
    var aInserer =
        ArretVoyage.creer(
            nouvelArret.id(),
            voyageId,
            apresIndiceArret + 1,
            nouvelArret.libelle(),
            nouvelArret.localisation(),
            nouvelArret.siteId(),
            nouvelArret.estOriginal());
    return sauvegarder(aInserer);
  }

  @Override
  public long compterParVoyageId(UUID voyageId) {
    return jpaRepository.countByVoyageId(voyageId);
  }

  @Override
  @Transactional
  public void supprimerParId(UUID arretId) {
    jpaRepository.deleteById(arretId);
  }

  @Override
  @Transactional
  public void reindexerSequences(UUID voyageId) {
    var entites = jpaRepository.findByVoyageIdOrderByIndiceSequenceAsc(voyageId);
    for (int i = 0; i < entites.size(); i++) {
      entites.get(i).mettreAJourIndiceSequence(i);
    }
    jpaRepository.saveAll(entites);
  }
}
