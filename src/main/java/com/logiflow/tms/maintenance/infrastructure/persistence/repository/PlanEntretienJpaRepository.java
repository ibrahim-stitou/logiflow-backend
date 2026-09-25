package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.PlanEntretienEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanEntretienJpaRepository extends JpaRepository<PlanEntretienEntity, UUID> {

  @Query(
      """
      SELECT e FROM PlanEntretienEntity e
      WHERE (:typeEngin IS NULL OR e.typeEngin = :typeEngin)
        AND (:enginId IS NULL OR e.enginId = :enginId)
        AND (:actif IS NULL OR e.actif = :actif)
      ORDER BY e.libelle
      """)
  Page<PlanEntretienEntity> rechercher(
      @Param("typeEngin") String typeEngin,
      @Param("enginId") UUID enginId,
      @Param("actif") Boolean actif,
      Pageable pageable);

  List<PlanEntretienEntity> findByActifTrue();
}
