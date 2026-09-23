package com.logiflow.tms.planning.infrastructure.persistence.repository;

import com.logiflow.tms.planning.infrastructure.persistence.entity.VoyageArretEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoyageArretJpaRepository extends JpaRepository<VoyageArretEntity, UUID> {

  List<VoyageArretEntity> findByVoyageIdOrderByIndiceSequenceAsc(UUID voyageId);

  @Modifying
  @Query("delete from VoyageArretEntity a where a.voyageId = :voyageId")
  void deleteByVoyageId(@Param("voyageId") UUID voyageId);

  long countByVoyageId(UUID voyageId);
}
