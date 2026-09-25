package com.logiflow.tms.maintenance.infrastructure.persistence.adapter;

import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.infrastructure.persistence.entity.OrdreTravailEntity;
import com.logiflow.tms.maintenance.infrastructure.persistence.mapper.MaintenanceMapper;
import com.logiflow.tms.maintenance.infrastructure.persistence.repository.OrdreTravailJpaRepository;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrdreTravailRepositoryAdapter implements OrdreTravailRepository {

  private final OrdreTravailJpaRepository jpaRepository;
  private final MaintenanceMapper mapper;

  @Override
  public OrdreTravail sauvegarder(OrdreTravail ordreTravail) {
    OrdreTravailEntity entite =
        jpaRepository
            .findById(ordreTravail.id())
            .orElseGet(() -> new OrdreTravailEntity(ordreTravail.id()));
    mapper.appliquer(entite, ordreTravail);
    return mapper.versDomaine(jpaRepository.save(entite));
  }

  @Override
  public Optional<OrdreTravail> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Page<OrdreTravail> rechercher(Filtre f, PageRequest pageRequest) {
    org.springframework.data.domain.Page<OrdreTravailEntity> resultat =
        jpaRepository.rechercher(
            nom(f.typeEngin()),
            f.enginId(),
            nom(f.statut()),
            nom(f.type()),
            nom(f.nature()),
            f.prestataireId(),
            texte(f.texte()),
            f.debut() == null ? OrdreTravailJpaRepository.DEBUT_MIN : f.debut(),
            f.fin() == null ? OrdreTravailJpaRepository.FIN_MAX : f.fin(),
            org.springframework.data.domain.PageRequest.of(
                pageRequest.numero(), pageRequest.taille()));
    return Page.of(
        resultat.getContent().stream().map(mapper::versDomaine).toList(),
        resultat.getNumber(),
        resultat.getSize(),
        resultat.getTotalElements());
  }

  @Override
  public List<OrdreTravail> lister(Filtre f) {
    return jpaRepository
        .lister(
            nom(f.typeEngin()),
            f.enginId(),
            nom(f.statut()),
            nom(f.type()),
            nom(f.nature()),
            f.prestataireId(),
            texte(f.texte()),
            f.debut() == null ? OrdreTravailJpaRepository.DEBUT_MIN : f.debut(),
            f.fin() == null ? OrdreTravailJpaRepository.FIN_MAX : f.fin())
        .stream()
        .map(mapper::versDomaine)
        .toList();
  }

  @Override
  public List<OrdreTravail> parPlanId(UUID planId) {
    return jpaRepository.findByPlanIdOrderByDebutPlanifieDesc(planId).stream()
        .map(mapper::versDomaine)
        .toList();
  }

  @Override
  public List<OrdreTravail> parSinistreId(UUID sinistreId) {
    return jpaRepository.findBySinistreIdOrderByDebutPlanifieDesc(sinistreId).stream()
        .map(mapper::versDomaine)
        .toList();
  }

  private static String nom(Enum<?> valeur) {
    return valeur == null ? null : valeur.name();
  }

  private static String texte(String valeur) {
    return valeur == null ? "" : valeur.strip();
  }
}
