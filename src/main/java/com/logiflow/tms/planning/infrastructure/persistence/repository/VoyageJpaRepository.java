package com.logiflow.tms.planning.infrastructure.persistence.repository;

import com.logiflow.tms.planning.infrastructure.persistence.entity.VoyageEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoyageJpaRepository extends JpaRepository<VoyageEntity, UUID> {

  Optional<VoyageEntity> findByReference(String reference);

  @Query(
      value =
          "SELECT * FROM planning.voyage v WHERE v.dossier_ids_json::jsonb @> CAST(:fragment AS jsonb)",
      nativeQuery = true)
  List<VoyageEntity> findByDossierId(@Param("fragment") String fragment);
}
