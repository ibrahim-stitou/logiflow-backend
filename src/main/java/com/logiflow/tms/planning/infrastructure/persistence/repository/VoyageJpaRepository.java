package com.logiflow.tms.planning.infrastructure.persistence.repository;

import com.logiflow.tms.planning.infrastructure.persistence.entity.VoyageEntity;
import jakarta.persistence.LockModeType;
import java.time.Instant;
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

  @Query(
      """
      SELECT e FROM VoyageEntity e
      WHERE (:statut IS NULL OR e.statut = :statut)
        AND (:q = '' OR LOWER(e.reference) LIKE LOWER(CONCAT('%', :q, '%')))
      ORDER BY e.updatedAt DESC
      """)
  Page<VoyageEntity> rechercherParStatut(
      @Param("q") String texteRecherche, @Param("statut") String statut, Pageable pageable);

  @Query(
      value =
          "SELECT * FROM planning.voyage v WHERE v.affectations_json::jsonb @> CAST(:fragment AS jsonb)"
              + " ORDER BY v.depart_prevu DESC",
      nativeQuery = true)
  List<VoyageEntity> findByChauffeurId(@Param("fragment") String fragment);

  @Query(
      """
      SELECT e FROM VoyageEntity e
      WHERE e.statut NOT IN ('ANNULE', 'TERMINE', 'CLOTURE')
        AND e.departPrevu < :fin
        AND e.arriveePrevue > :debut
      """)
  List<VoyageEntity> actifsSurPeriode(@Param("debut") Instant debut, @Param("fin") Instant fin);

  @Query(
      """
      SELECT e FROM VoyageEntity e
      WHERE e.statut <> 'ANNULE' AND e.departPrevu < :fin AND e.arriveePrevue > :debut
      """)
  List<VoyageEntity> nonAnnulesSurPeriode(@Param("debut") Instant debut, @Param("fin") Instant fin);
}
