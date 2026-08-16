package com.logiflow.tms.fleet.infrastructure.persistence.repository;

import com.logiflow.tms.fleet.infrastructure.persistence.entity.VehiculeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculeJpaRepository extends JpaRepository<VehiculeEntity, UUID> {

  Optional<VehiculeEntity> findByImmatriculation(String immatriculation);

  boolean existsByImmatriculation(String immatriculation);

  Page<VehiculeEntity> findByImmatriculationContainingIgnoreCase(
      String immatriculation, Pageable pageable);
}
