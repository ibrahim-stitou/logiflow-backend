package com.logiflow.tms.fleet.infrastructure.persistence.repository;

import com.logiflow.tms.fleet.infrastructure.persistence.entity.RemorqueEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RemorqueJpaRepository extends JpaRepository<RemorqueEntity, UUID> {

  Optional<RemorqueEntity> findByImmatriculation(String immatriculation);

  boolean existsByImmatriculation(String immatriculation);

  Page<RemorqueEntity> findByImmatriculationContainingIgnoreCase(
      String immatriculation, Pageable pageable);

  @Query(
      """
      SELECT e FROM RemorqueEntity e
      WHERE (:statut IS NULL OR e.statut = :statut)
        AND (:q = '' OR LOWER(e.immatriculation) LIKE LOWER(CONCAT('%', :q, '%')))
      ORDER BY e.updatedAt DESC
      """)
  Page<RemorqueEntity> rechercherParStatut(
      @Param("q") String texteRecherche, @Param("statut") String statut, Pageable pageable);
}
