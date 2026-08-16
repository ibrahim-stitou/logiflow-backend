package com.logiflow.tms.maintenance.infrastructure.persistence.adapter;

import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.infrastructure.persistence.mapper.OrdreTravailMapper;
import com.logiflow.tms.maintenance.infrastructure.persistence.repository.OrdreTravailJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrdreTravailRepositoryAdapter implements OrdreTravailRepository {

  private final OrdreTravailJpaRepository jpaRepository;
  private final OrdreTravailMapper mapper;

  @Override
  public OrdreTravail sauvegarder(OrdreTravail ordreTravail) {
    return mapper.versDomaine(jpaRepository.save(mapper.versEntite(ordreTravail)));
  }

  @Override
  public Optional<OrdreTravail> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public List<OrdreTravail> parVehiculeId(UUID vehiculeId) {
    return jpaRepository.findByVehiculeId(vehiculeId).stream().map(mapper::versDomaine).toList();
  }

  @Override
  public Page<OrdreTravail> rechercher(PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    var pageJpa = jpaRepository.findAll(pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
