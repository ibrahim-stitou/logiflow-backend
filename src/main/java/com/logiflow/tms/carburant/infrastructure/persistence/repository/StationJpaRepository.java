package com.logiflow.tms.carburant.infrastructure.persistence.repository;

import com.logiflow.tms.carburant.infrastructure.persistence.entity.StationEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationJpaRepository extends JpaRepository<StationEntity, UUID> {

  boolean existsByCode(String code);

  Optional<StationEntity> findByCode(String code);

  Page<StationEntity> findByLibelleContainingIgnoreCaseOrCodeContainingIgnoreCase(
      String libelle, String code, Pageable pageable);
}
