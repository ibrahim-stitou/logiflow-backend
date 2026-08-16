package com.logiflow.tms.maintenance.infrastructure.persistence.adapter;

import com.logiflow.tms.maintenance.domain.model.ScoreSante;
import com.logiflow.tms.maintenance.domain.port.out.ScoreSanteRepository;
import com.logiflow.tms.maintenance.infrastructure.persistence.mapper.ScoreSanteMapper;
import com.logiflow.tms.maintenance.infrastructure.persistence.repository.ScoreSanteJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreSanteRepositoryAdapter implements ScoreSanteRepository {

  private final ScoreSanteJpaRepository jpaRepository;
  private final ScoreSanteMapper mapper;

  @Override
  public ScoreSante sauvegarder(ScoreSante scoreSante) {
    return mapper.versDomaine(jpaRepository.save(mapper.versEntite(scoreSante)));
  }

  @Override
  public Optional<ScoreSante> parId(UUID id) {
    return jpaRepository.findById(id).map(mapper::versDomaine);
  }

  @Override
  public Optional<ScoreSante> dernierParVehiculeId(UUID vehiculeId) {
    return jpaRepository
        .findFirstByVehiculeIdOrderByCalculeLeDesc(vehiculeId)
        .map(mapper::versDomaine);
  }

  @Override
  public List<ScoreSante> parVehiculeId(UUID vehiculeId) {
    return jpaRepository.findByVehiculeId(vehiculeId).stream().map(mapper::versDomaine).toList();
  }
}
