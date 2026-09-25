package com.logiflow.tms.maintenance.infrastructure.persistence.adapter;

import com.logiflow.tms.maintenance.domain.model.ContratAssurance;
import com.logiflow.tms.maintenance.domain.port.out.ContratAssuranceRepository;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.ContratAssuranceEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.mapper.MaintenanceMapper;
import com.logiflow.tms.maintenance.infrastructure.persistence.repository.ContratAssuranceJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContratAssuranceRepositoryAdapter implements ContratAssuranceRepository {

  private final ContratAssuranceJpaRepository jpaRepository;
  private final MaintenanceMapper mapper;

  @Override
  public ContratAssurance sauvegarder(ContratAssurance contrat) {
    ContratAssuranceEntity entite =
        jpaRepository
            .findById(contrat.id())
            .orElseGet(() -> new ContratAssuranceEntity(contrat.id()));
    mapper.appliquer(entite, contrat);
    return mapper.versDomaine(jpaRepository.save(entite));
  }

  @Override
  public Optional<ContratAssurance> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Page<ContratAssurance> rechercher(Boolean actif, PageRequest pageRequest) {
    org.springframework.data.domain.Page<ContratAssuranceEntity> resultat =
        jpaRepository.rechercher(
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
  public List<ContratAssurance> actifs() {
    return jpaRepository.findByActifTrue().stream().map(mapper::versDomaine).toList();
  }
}
