package com.logiflow.tms.maintenance.infrastructure.persistence.adapter;

import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.port.out.PlanEntretienRepository;
import com.logiflow.tms.maintenance.infrastructure.persistence.mapper.PlanEntretienMapper;
import com.logiflow.tms.maintenance.infrastructure.persistence.repository.PlanEntretienJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanEntretienRepositoryAdapter implements PlanEntretienRepository {

  private final PlanEntretienJpaRepository jpaRepository;
  private final PlanEntretienMapper mapper;

  @Override
  public PlanEntretien sauvegarder(PlanEntretien plan) {
    return mapper.versDomaine(jpaRepository.save(mapper.versEntite(plan)));
  }

  @Override
  public Optional<PlanEntretien> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public List<PlanEntretien> parVehiculeId(UUID vehiculeId) {
    return jpaRepository.findByVehiculeId(vehiculeId).stream().map(mapper::versDomaine).toList();
  }
}
