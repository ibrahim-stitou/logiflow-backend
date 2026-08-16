package com.logiflow.tms.fleet.infrastructure.persistence.repository;

import com.logiflow.tms.fleet.infrastructure.persistence.entity.RemorqueEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemorqueJpaRepository extends JpaRepository<RemorqueEntity, UUID> {

  Optional<RemorqueEntity> findByImmatriculation(String immatriculation);

  boolean existsByImmatriculation(String immatriculation);
}
