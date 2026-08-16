package com.logiflow.tms.driver.infrastructure.persistence.adapter;

import com.logiflow.tms.driver.domain.model.Chauffeur;
import com.logiflow.tms.driver.domain.port.out.ChauffeurRepository;
import com.logiflow.tms.driver.infrastructure.persistence.entity.ChauffeurEntity;
import com.logiflow.tms.driver.infrastructure.persistence.mapper.ChauffeurMapper;
import com.logiflow.tms.driver.infrastructure.persistence.repository.ChauffeurJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChauffeurRepositoryAdapter implements ChauffeurRepository {

  private final ChauffeurJpaRepository jpaRepository;
  private final ChauffeurMapper mapper;

  @Override
  public Chauffeur sauvegarder(Chauffeur chauffeur) {
    var entite = jpaRepository.save(mapper.versEntite(chauffeur));
    return mapper.versDomaine(entite);
  }

  @Override
  public Optional<Chauffeur> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Chauffeur> parMatricule(String matricule) {
    return jpaRepository.findByMatricule(matricule).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParMatricule(String matricule) {
    return jpaRepository.existsByMatricule(matricule);
  }

  @Override
  public Page<Chauffeur> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<ChauffeurEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByNomCompletContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
                texteRecherche, texteRecherche, pageable);

    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }
}
