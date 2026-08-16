package com.logiflow.tms.planning.infrastructure.persistence.adapter;

import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.infrastructure.persistence.mapper.VoyageMapper;
import com.logiflow.tms.planning.infrastructure.persistence.repository.VoyageJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoyageRepositoryAdapter implements VoyageRepository {

  private final VoyageJpaRepository jpaRepository;
  private final VoyageMapper mapper;

  @Override
  public Voyage sauvegarder(Voyage voyage) {
    var entite = jpaRepository.save(mapper.versEntite(voyage));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Voyage> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Voyage> parReference(String reference) {
    return jpaRepository.findByReference(reference).map(mapper::versDomaine);
  }

  @Override
  public Page<Voyage> rechercher(PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    var pageJpa = jpaRepository.findAll(pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
