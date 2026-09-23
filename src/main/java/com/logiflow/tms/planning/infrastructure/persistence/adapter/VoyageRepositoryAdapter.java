package com.logiflow.tms.planning.infrastructure.persistence.adapter;

import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.infrastructure.persistence.entity.VoyageEntity;
import com.logiflow.tms.planning.infrastructure.persistence.mapper.VoyageMapper;
import com.logiflow.tms.planning.infrastructure.persistence.repository.VoyageJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
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
    var entite =
        jpaRepository
            .findById(voyage.id())
            .map(
                existante -> {
                  mapper.mettreAJour(existante, voyage);
                  return existante;
                })
            .orElseGet(() -> mapper.versEntite(voyage));
    return mapper.versDomaine(jpaRepository.save(entite));
  }

  @Override
  public Optional<Voyage> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Voyage> parIdAvecVerrouillage(UUID id) {
    return jpaRepository.findByIdForUpdate(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<Voyage> parReference(String reference) {
    return jpaRepository.findByReference(reference).map(mapper::versDomaine);
  }

  @Override
  public Page<Voyage> rechercher(String texteRecherche, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<VoyageEntity> pageJpa =
        (texteRecherche == null || texteRecherche.isBlank())
            ? jpaRepository.findAll(pageable)
            : jpaRepository.findByReferenceContainingIgnoreCase(texteRecherche, pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }

  @Override
  public Page<Voyage> rechercherParStatut(
      String texteRecherche, String statut, PageRequest pageRequest) {
    var pageable =
        org.springframework.data.domain.PageRequest.of(pageRequest.numero(), pageRequest.taille());
    org.springframework.data.domain.Page<VoyageEntity> pageJpa =
        jpaRepository.rechercherParStatut(
            texteRecherche == null ? "" : texteRecherche.strip(), statut, pageable);
    var contenu = pageJpa.getContent().stream().map(mapper::versDomaine).toList();
    return Page.of(contenu, pageJpa.getNumber(), pageJpa.getSize(), pageJpa.getTotalElements());
  }

  @Override
  public List<Voyage> parDossierId(UUID dossierId) {
    String fragment = "[\"" + dossierId + "\"]";
    return jpaRepository.findByDossierId(fragment).stream().map(mapper::versDomaine).toList();
  }
}
