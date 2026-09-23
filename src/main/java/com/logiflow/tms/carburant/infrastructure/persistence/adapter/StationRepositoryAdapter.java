package com.logiflow.tms.carburant.infrastructure.persistence.adapter;

import com.logiflow.tms.carburant.domain.model.Station;
import com.logiflow.tms.carburant.domain.port.out.StationRepository;
import com.logiflow.tms.carburant.infrastructure.persistence.entity.StationEntity;
import com.logiflow.tms.carburant.infrastructure.persistence.mapper.StationMapper;
import com.logiflow.tms.carburant.infrastructure.persistence.repository.StationJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StationRepositoryAdapter implements StationRepository {

  private final StationJpaRepository jpaRepository;
  private final StationMapper mapper;

  @Override
  public Station sauvegarder(Station station) {
    StationEntity entite =
        jpaRepository
            .findById(station.id())
            .map(
                existante -> {
                  mapper.mettreAJour(existante, station);
                  return existante;
                })
            .orElseGet(() -> mapper.versEntite(station));
    return mapper.versDomaine(jpaRepository.save(entite));
  }

  @Override
  public Optional<Station> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public Page<Station> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<StationEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByLibelleContainingIgnoreCaseOrCodeContainingIgnoreCase(
                texteRecherche, texteRecherche, pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
