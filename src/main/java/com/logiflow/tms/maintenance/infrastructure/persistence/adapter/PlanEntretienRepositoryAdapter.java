package com.logiflow.tms.maintenance.infrastructure.persistence.adapter;

import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.port.out.PlanEntretienRepository;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.PlanEntretienEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.mapper.MaintenanceMapper;
import com.logiflow.tms.maintenance.infrastructure.persistence.repository.PlanEntretienJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanEntretienRepositoryAdapter implements PlanEntretienRepository {

  private final PlanEntretienJpaRepository jpaRepository;
  private final MaintenanceMapper mapper;

  @Override
  public PlanEntretien sauvegarder(PlanEntretien plan) {
    PlanEntretienEntity entite =
        jpaRepository.findById(plan.id()).orElseGet(() -> new PlanEntretienEntity(plan.id()));
    mapper.appliquer(entite, plan);
    return mapper.versDomaine(jpaRepository.save(entite));
  }

  @Override
  public Optional<PlanEntretien> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Page<PlanEntretien> rechercher(
      TypeEngin typeEngin, UUID enginId, Boolean actif, PageRequest pageRequest) {
    org.springframework.data.domain.Page<PlanEntretienEntity> resultat =
        jpaRepository.rechercher(
            typeEngin == null ? null : typeEngin.name(),
            enginId,
            actif,
            org.springframework.data.domain.PageRequest.of(
                pageRequest.numero(), pageRequest.taille()));
    return Page.of(
        resultat.getContent().stream().map(mapper::versDomaine).toList(),
        resultat.getNumber(),
        resultat.getSize(),
        resultat.getTotalElements());
  }

  @Override
  public List<PlanEntretien> actifs() {
    return jpaRepository.findByActifTrue().stream().map(mapper::versDomaine).toList();
  }
}
