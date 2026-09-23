package com.logiflow.tms.carburant.infrastructure.persistence.repository;

import com.logiflow.tms.carburant.infrastructure.persistence.entity.PriseCarburantEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriseCarburantJpaRepository extends JpaRepository<PriseCarburantEntity, UUID> {

  @Query(
      """
      SELECT p FROM PriseCarburantEntity p
      JOIN StationEntity s ON p.stationId = s.id
      WHERE (:voyageId IS NULL OR p.voyageId = :voyageId)
        AND (:statut IS NULL OR p.statut = :statut)
        AND (
          :q IS NULL OR :q = ''
          OR LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(s.libelle) LIKE LOWER(CONCAT('%', :q, '%'))
        )
      ORDER BY p.datePrise DESC
      """)
  Page<PriseCarburantEntity> rechercher(
      @Param("q") String texteRecherche,
      @Param("voyageId") UUID voyageId,
      @Param("statut") String statut,
      Pageable pageable);

  @Query(
      """
      SELECT COUNT(p), COALESCE(SUM(p.litrage), 0), COALESCE(SUM(p.montantTtc), 0)
      FROM PriseCarburantEntity p
      JOIN StationEntity s ON p.stationId = s.id
      WHERE (:voyageId IS NULL OR p.voyageId = :voyageId)
        AND (:statut IS NULL OR p.statut = :statut)
        AND (
          :q IS NULL OR :q = ''
          OR LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(s.libelle) LIKE LOWER(CONCAT('%', :q, '%'))
        )
      """)
  Object[] agregerTotaux(
      @Param("q") String texteRecherche,
      @Param("voyageId") UUID voyageId,
      @Param("statut") String statut);

  @Query(
      """
      SELECT p.typeCarburant, COUNT(p), COALESCE(SUM(p.litrage), 0), COALESCE(SUM(p.montantTtc), 0)
      FROM PriseCarburantEntity p
      JOIN StationEntity s ON p.stationId = s.id
      WHERE (:voyageId IS NULL OR p.voyageId = :voyageId)
        AND (:statut IS NULL OR p.statut = :statut)
        AND (
          :q IS NULL OR :q = ''
          OR LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(s.libelle) LIKE LOWER(CONCAT('%', :q, '%'))
        )
      GROUP BY p.typeCarburant
      """)
  java.util.List<Object[]> agregerParType(
      @Param("q") String texteRecherche,
      @Param("voyageId") UUID voyageId,
      @Param("statut") String statut);

  @Query(
      """
      SELECT COUNT(p), COALESCE(SUM(p.litrage), 0), COALESCE(SUM(p.montantTtc), 0)
      FROM PriseCarburantEntity p
      WHERE (:vehiculeId IS NULL OR p.vehiculeId = :vehiculeId)
        AND p.datePrise >= :debut AND p.datePrise < :fin
      """)
  Object[] agregerTotauxPeriode(
      @Param("vehiculeId") UUID vehiculeId,
      @Param("debut") java.time.Instant debut,
      @Param("fin") java.time.Instant fin);

  @Query(
      """
      SELECT p.typeCarburant, COUNT(p), COALESCE(SUM(p.litrage), 0), COALESCE(SUM(p.montantTtc), 0)
      FROM PriseCarburantEntity p
      WHERE (:vehiculeId IS NULL OR p.vehiculeId = :vehiculeId)
        AND p.datePrise >= :debut AND p.datePrise < :fin
      GROUP BY p.typeCarburant
      """)
  java.util.List<Object[]> agregerParTypePeriode(
      @Param("vehiculeId") UUID vehiculeId,
      @Param("debut") java.time.Instant debut,
      @Param("fin") java.time.Instant fin);
}
