package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.SinistreEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SinistreJpaRepository extends JpaRepository<SinistreEntity, UUID> {

  String FILTRE =
      """
      FROM SinistreEntity e
      WHERE (:enginId IS NULL OR e.vehiculeId = :enginId OR e.remorqueId = :enginId)
        AND (:chauffeurId IS NULL OR e.chauffeurId = :chauffeurId)
        AND (:statut IS NULL OR e.statut = :statut)
        AND (:type IS NULL OR e.typeSinistre = :type)
        AND (:q = '' OR LOWER(e.reference) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(e.lieu) LIKE LOWER(CONCAT('%', :q, '%')))
        AND e.dateSurvenance >= :debut
        AND e.dateSurvenance < :fin
      """;

  /** Bornes par défaut quand la période n'est pas filtrée (paramètres jamais null). */
  LocalDateTime DEBUT_MIN = LocalDateTime.of(1900, 1, 1, 0, 0);

  LocalDateTime FIN_MAX = LocalDateTime.of(9999, 12, 31, 0, 0);

  @Query(
      value = "SELECT e " + FILTRE + " ORDER BY e.dateSurvenance DESC",
      countQuery = "SELECT COUNT(e) " + FILTRE)
  Page<SinistreEntity> rechercher(
      @Param("enginId") UUID enginId,
      @Param("chauffeurId") UUID chauffeurId,
      @Param("statut") String statut,
      @Param("type") String type,
      @Param("q") String texte,
      @Param("debut") LocalDateTime debut,
      @Param("fin") LocalDateTime fin,
      Pageable pageable);

  @Query("SELECT e " + FILTRE + " ORDER BY e.dateSurvenance DESC")
  List<SinistreEntity> lister(
      @Param("enginId") UUID enginId,
      @Param("chauffeurId") UUID chauffeurId,
      @Param("statut") String statut,
      @Param("type") String type,
      @Param("q") String texte,
      @Param("debut") LocalDateTime debut,
      @Param("fin") LocalDateTime fin);
}
