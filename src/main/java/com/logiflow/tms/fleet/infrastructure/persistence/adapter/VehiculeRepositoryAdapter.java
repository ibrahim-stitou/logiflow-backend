package com.logiflow.tms.fleet.infrastructure.persistence.adapter;

import com.logiflow.tms.fleet.domain.model.Vehicule;
import com.logiflow.tms.fleet.domain.port.out.VehiculeRepository;
import com.logiflow.tms.fleet.infrastructure.persistence.entity.VehiculeEntity;
import com.logiflow.tms.fleet.infrastructure.persistence.mapper.VehiculeMapper;
import com.logiflow.tms.fleet.infrastructure.persistence.repository.VehiculeJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehiculeRepositoryAdapter implements VehiculeRepository {

  private final VehiculeJpaRepository jpaRepository;
  private final VehiculeMapper mapper;

  @Override
  public Vehicule sauvegarder(Vehicule vehicule) {
    var entite = jpaRepository.save(mapper.versEntite(vehicule));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Vehicule> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Vehicule> parImmatriculation(String immatriculation) {
    return jpaRepository.findByImmatriculation(immatriculation).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParImmatriculation(String immatriculation) {
    return jpaRepository.existsByImmatriculation(immatriculation);
  }

  @Override
  public Page<Vehicule> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<VehiculeEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByImmatriculationContainingIgnoreCase(texteRecherche, pageable);

    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
