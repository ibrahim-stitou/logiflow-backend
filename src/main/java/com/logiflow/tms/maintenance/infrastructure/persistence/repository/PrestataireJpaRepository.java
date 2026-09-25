package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.PrestataireEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrestataireJpaRepository extends JpaRepository<PrestataireEntity, UUID> {

  boolean existsByCode(String code);

  @Query(
      """
      SELECT e FROM PrestataireEntity e
      WHERE (:type IS NULL OR e.typePrestataire = :type)
        AND (:actif IS NULL OR e.actif = :actif)
        AND (:q = '' OR LOWER(e.raisonSociale) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(e.code) LIKE LOWER(CONCAT('%', :q, '%')))
      ORDER BY e.raisonSociale
      """)
  Page<PrestataireEntity> rechercher(
      @Param("q") String texte,
      @Param("type") String type,
      @Param("actif") Boolean actif,
      Pageable pageable);
}
