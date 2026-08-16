package com.logiflow.tms.fleet.infrastructure.persistence.adapter;

import com.logiflow.tms.fleet.domain.model.Remorque;
import com.logiflow.tms.fleet.domain.port.out.RemorqueRepository;
import com.logiflow.tms.fleet.infrastructure.persistence.mapper.RemorqueMapper;
import com.logiflow.tms.fleet.infrastructure.persistence.repository.RemorqueJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemorqueRepositoryAdapter implements RemorqueRepository {

  private final RemorqueJpaRepository jpaRepository;
  private final RemorqueMapper mapper;

  @Override
  public Remorque sauvegarder(Remorque remorque) {
    var entite = jpaRepository.save(mapper.versEntite(remorque));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Remorque> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Remorque> parImmatriculation(String immatriculation) {
    return jpaRepository.findByImmatriculation(immatriculation).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParImmatriculation(String immatriculation) {
    return jpaRepository.existsByImmatriculation(immatriculation);
  }

  @Override
  public Page<Remorque> rechercher(PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    var pageJpa = jpaRepository.findAll(pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
