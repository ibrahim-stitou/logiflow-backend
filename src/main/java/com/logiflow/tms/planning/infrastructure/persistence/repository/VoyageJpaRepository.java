package com.logiflow.tms.planning.infrastructure.persistence.repository;

import com.logiflow.tms.planning.infrastructure.persistence.entity.VoyageEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoyageJpaRepository extends JpaRepository<VoyageEntity, UUID> {

  Optional<VoyageEntity> findByReference(String reference);
}
