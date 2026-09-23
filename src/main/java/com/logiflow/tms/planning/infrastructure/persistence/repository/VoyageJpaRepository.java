package com.logiflow.tms.planning.infrastructure.persistence.repository;

import com.logiflow.tms.planning.infrastructure.persistence.entity.VoyageEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoyageJpaRepository extends JpaRepository<VoyageEntity, UUID> {

  Optional<VoyageEntity> findByReference(String reference);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM VoyageEntity v WHERE v.id = :id")
  Optional<VoyageEntity> findByIdForUpdate(@Param("id") UUID id);

  Page<VoyageEntity> findByReferenceContainingIgnoreCase(String reference, Pageable pageable);

  @Query(
      value =
          "SELECT * FROM planning.voyage v WHERE v.dossier_ids_json::jsonb @> CAST(:fragment AS jsonb)",
      nativeQuery = true)
  List<VoyageEntity> findByDossierId(@Param("fragment") String fragment);
}
