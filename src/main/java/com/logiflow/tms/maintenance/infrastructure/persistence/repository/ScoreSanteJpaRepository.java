package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.ScoreSanteEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreSanteJpaRepository extends JpaRepository<ScoreSanteEntity, UUID> {

  List<ScoreSanteEntity> findByVehiculeId(UUID vehiculeId);

  Optional<ScoreSanteEntity> findFirstByVehiculeIdOrderByCalculeLeDesc(UUID vehiculeId);
}
