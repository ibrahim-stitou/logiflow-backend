package com.logiflow.tms.referential.infrastructure.persistence.adapter;

import com.logiflow.tms.referential.domain.model.Site;
import com.logiflow.tms.referential.domain.port.out.SiteRepository;
import com.logiflow.tms.referential.infrastructure.persistence.entity.SiteEntity;
import com.logiflow.tms.referential.infrastructure.persistence.mapper.SiteMapper;
import com.logiflow.tms.referential.infrastructure.persistence.repository.SiteJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SiteRepositoryAdapter implements SiteRepository {

  private final SiteJpaRepository jpaRepository;
  private final SiteMapper mapper;

  @Override
  public Site sauvegarder(Site site) {
    var entite = jpaRepository.save(mapper.versEntite(site));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Site> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Site> parCode(String code) {
    return jpaRepository.findByCode(code).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public Page<Site> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<SiteEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByLibelleContainingIgnoreCaseOrCodeContainingIgnoreCase(
                texteRecherche, texteRecherche, pageable);

    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
