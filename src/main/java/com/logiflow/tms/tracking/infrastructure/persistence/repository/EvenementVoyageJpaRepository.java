package com.logiflow.tms.tracking.infrastructure.persistence.repository;

import com.logiflow.tms.tracking.infrastructure.persistence.entity.EvenementVoyageEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvenementVoyageJpaRepository extends JpaRepository<EvenementVoyageEntity, UUID> {

  List<EvenementVoyageEntity> findByVoyageIdOrderByHorodatageAsc(UUID voyageId);

  Optional<EvenementVoyageEntity> findFirstByVoyageIdOrderByHorodatageDesc(UUID voyageId);
}
