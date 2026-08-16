package com.logiflow.tms.tracking.infrastructure.persistence.adapter;

import com.logiflow.tms.tracking.domain.model.EvenementVoyage;
import com.logiflow.tms.tracking.domain.port.out.EvenementVoyageRepository;
import com.logiflow.tms.tracking.infrastructure.persistence.mapper.EvenementVoyageMapper;
import com.logiflow.tms.tracking.infrastructure.persistence.repository.EvenementVoyageJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvenementVoyageRepositoryAdapter implements EvenementVoyageRepository {

  private final EvenementVoyageJpaRepository jpaRepository;
  private final EvenementVoyageMapper mapper;

  @Override
  public EvenementVoyage sauvegarder(EvenementVoyage evenement) {
    return mapper.versDomaine(jpaRepository.save(mapper.versEntite(evenement)));
  }

  @Override
  public Optional<EvenementVoyage> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public List<EvenementVoyage> parVoyageId(UUID voyageId) {
    return jpaRepository.findByVoyageIdOrderByHorodatageAsc(voyageId).stream()
        .map(mapper::versDomaine)
        .toList();
  }

  @Override
  public Optional<EvenementVoyage> dernierParVoyageId(UUID voyageId) {
    return jpaRepository
        .findFirstByVoyageIdOrderByHorodatageDesc(voyageId)
        .map(mapper::versDomaine);
  }
}
