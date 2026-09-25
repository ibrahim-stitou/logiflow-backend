package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.ContratAssuranceEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContratAssuranceJpaRepository extends JpaRepository<ContratAssuranceEntity, UUID> {

  @Query(
      """
      SELECT e FROM ContratAssuranceEntity e
      WHERE (:actif IS NULL OR e.actif = :actif)
      ORDER BY e.dateEcheance
      """)
  Page<ContratAssuranceEntity> rechercher(@Param("actif") Boolean actif, Pageable pageable);

  List<ContratAssuranceEntity> findByActifTrue();
}
