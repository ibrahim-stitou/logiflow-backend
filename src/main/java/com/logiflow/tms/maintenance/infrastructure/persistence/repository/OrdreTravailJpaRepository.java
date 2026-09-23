package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.OrdreTravailEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdreTravailJpaRepository extends JpaRepository<OrdreTravailEntity, UUID> {

  List<OrdreTravailEntity> findByVehiculeId(UUID vehiculeId);

  Page<OrdreTravailEntity> findByVehiculeId(UUID vehiculeId, Pageable pageable);

  @Query(
      """
      SELECT COUNT(o),
             COALESCE(SUM(o.coutMontant), 0),
             COALESCE(SUM(CASE WHEN o.statut = 'EN_COURS' THEN 1 ELSE 0 END), 0)
      FROM OrdreTravailEntity o
      WHERE (:vehiculeId IS NULL OR o.vehiculeId = :vehiculeId)
        AND (:statut IS NULL OR o.statut = :statut)
      """)
  java.util.List<Object[]> agregerTotaux(
      @Param("vehiculeId") UUID vehiculeId, @Param("statut") String statut);
}
