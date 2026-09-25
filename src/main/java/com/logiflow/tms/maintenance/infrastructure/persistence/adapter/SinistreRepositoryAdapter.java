package com.logiflow.tms.maintenance.infrastructure.persistence.adapter;

import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.port.out.SinistreRepository;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.SinistreEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.mapper.MaintenanceMapper;
import com.logiflow.tms.maintenance.infrastructure.persistence.repository.SinistreJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SinistreRepositoryAdapter implements SinistreRepository {

  private final SinistreJpaRepository jpaRepository;
  private final MaintenanceMapper mapper;

  @Override
  public Sinistre sauvegarder(Sinistre sinistre) {
    SinistreEntity entite =
        jpaRepository.findById(sinistre.id()).orElseGet(() -> new SinistreEntity(sinistre.id()));
    mapper.appliquer(entite, sinistre);
    return mapper.versDomaine(jpaRepository.save(entite));
  }

  @Override
  public Optional<Sinistre> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Page<Sinistre> rechercher(Filtre f, PageRequest pageRequest) {
    org.springframework.data.domain.Page<SinistreEntity> resultat =
        jpaRepository.rechercher(
            f.enginId(),
            f.chauffeurId(),
            f.statut() == null ? null : f.statut().name(),
            f.type() == null ? null : f.type().name(),
            f.texte() == null ? "" : f.texte().strip(),
            f.debut() == null ? SinistreJpaRepository.DEBUT_MIN : f.debut(),
            f.fin() == null ? SinistreJpaRepository.FIN_MAX : f.fin(),
            org.springframework.data.domain.PageRequest.of(
                pageRequest.numero(), pageRequest.taille()));
    return Page.of(
        resultat.getContent().stream().map(mapper::versDomaine).toList(),
        resultat.getNumber(),
        resultat.getSize(),
        resultat.getTotalElements());
  }

  @Override
  public List<Sinistre> lister(Filtre f) {
    return jpaRepository
        .lister(
            f.enginId(),
            f.chauffeurId(),
            f.statut() == null ? null : f.statut().name(),
            f.type() == null ? null : f.type().name(),
            f.texte() == null ? "" : f.texte().strip(),
            f.debut() == null ? SinistreJpaRepository.DEBUT_MIN : f.debut(),
            f.fin() == null ? SinistreJpaRepository.FIN_MAX : f.fin())
        .stream()
        .map(mapper::versDomaine)
        .toList();
  }
}
