package com.logiflow.tms.referential.infrastructure.persistence.adapter;

import com.logiflow.tms.referential.domain.model.Marchandise;
import com.logiflow.tms.referential.domain.port.out.MarchandiseRepository;
import com.logiflow.tms.referential.infrastructure.persistence.entity.MarchandiseEntity;
import com.logiflow.tms.referential.infrastructure.persistence.mapper.MarchandiseMapper;
import com.logiflow.tms.referential.infrastructure.persistence.repository.MarchandiseJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarchandiseRepositoryAdapter implements MarchandiseRepository {

  private final MarchandiseJpaRepository jpaRepository;
  private final MarchandiseMapper mapper;

  @Override
  public Marchandise sauvegarder(Marchandise marchandise) {
    var entite = jpaRepository.save(mapper.versEntite(marchandise));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Marchandise> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Marchandise> parCode(String code) {
    return jpaRepository.findByCode(code).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public Page<Marchandise> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<MarchandiseEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByLibelleContainingIgnoreCaseOrCodeContainingIgnoreCase(
                texteRecherche, texteRecherche, pageable);

    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
