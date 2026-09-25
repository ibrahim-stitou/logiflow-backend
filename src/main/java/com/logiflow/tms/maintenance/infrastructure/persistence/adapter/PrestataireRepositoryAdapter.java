package com.logiflow.tms.maintenance.infrastructure.persistence.adapter;

import com.logiflow.tms.maintenance.domain.model.Prestataire;
import com.logiflow.tms.maintenance.domain.model.TypePrestataire;
import com.logiflow.tms.maintenance.domain.port.out.PrestataireRepository;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.PrestataireEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.mapper.MaintenanceMapper;
import com.logiflow.tms.maintenance.infrastructure.persistence.repository.PrestataireJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrestataireRepositoryAdapter implements PrestataireRepository {

  private final PrestataireJpaRepository jpaRepository;
  private final MaintenanceMapper mapper;

  @Override
  public Prestataire sauvegarder(Prestataire prestataire) {
    PrestataireEntity entite =
        jpaRepository
            .findById(prestataire.id())
            .orElseGet(() -> new PrestataireEntity(prestataire.id()));
    mapper.appliquer(entite, prestataire);
    return mapper.versDomaine(jpaRepository.save(entite));
  }

  @Override
  public Optional<Prestataire> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public boolean existeParCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public Page<Prestataire> rechercher(
      String texte, TypePrestataire type, Boolean actif, PageRequest pageRequest) {
    org.springframework.data.domain.Page<PrestataireEntity> resultat =
        jpaRepository.rechercher(
            texte == null ? "" : texte.strip(),
            type == null ? null : type.name(),
            actif,
            org.springframework.data.domain.PageRequest.of(
                pageRequest.numero(), pageRequest.taille()));
    return Page.of(
        resultat.getContent().stream().map(mapper::versDomaine).toList(),
        resultat.getNumber(),
        resultat.getSize(),
        resultat.getTotalElements());
  }
}
