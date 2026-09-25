package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.OrdreTravailEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdreTravailJpaRepository extends JpaRepository<OrdreTravailEntity, UUID> {

  String FILTRE =
      """
      FROM OrdreTravailEntity e
      WHERE (:typeEngin IS NULL OR e.typeEngin = :typeEngin)
        AND (:enginId IS NULL OR e.enginId = :enginId)
        AND (:statut IS NULL OR e.statut = :statut)
        AND (:type IS NULL OR e.typeIntervention = :type)
        AND (:nature IS NULL OR e.nature = :nature)
        AND (:prestataireId IS NULL OR e.prestataireId = :prestataireId)
        AND (:q = '' OR LOWER(e.reference) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(e.titre) LIKE LOWER(CONCAT('%', :q, '%')))
        AND e.debutPlanifie >= :debut
        AND e.debutPlanifie < :fin
      """;

  /** Bornes par défaut quand la période n'est pas filtrée (paramètres jamais null). */
  LocalDateTime DEBUT_MIN = LocalDateTime.of(1900, 1, 1, 0, 0);

  LocalDateTime FIN_MAX = LocalDateTime.of(9999, 12, 31, 0, 0);

  @Query(
      value = "SELECT e " + FILTRE + " ORDER BY e.debutPlanifie DESC",
      countQuery = "SELECT COUNT(e) " + FILTRE)
  Page<OrdreTravailEntity> rechercher(
      @Param("typeEngin") String typeEngin,
      @Param("enginId") UUID enginId,
      @Param("statut") String statut,
      @Param("type") String type,
      @Param("nature") String nature,
      @Param("prestataireId") UUID prestataireId,
      @Param("q") String texte,
      @Param("debut") LocalDateTime debut,
      @Param("fin") LocalDateTime fin,
      Pageable pageable);

  @Query("SELECT e " + FILTRE + " ORDER BY e.debutPlanifie DESC")
  List<OrdreTravailEntity> lister(
      @Param("typeEngin") String typeEngin,
      @Param("enginId") UUID enginId,
      @Param("statut") String statut,
      @Param("type") String type,
      @Param("nature") String nature,
      @Param("prestataireId") UUID prestataireId,
      @Param("q") String texte,
      @Param("debut") LocalDateTime debut,
      @Param("fin") LocalDateTime fin);

  List<OrdreTravailEntity> findByPlanIdOrderByDebutPlanifieDesc(UUID planId);

  List<OrdreTravailEntity> findBySinistreIdOrderByDebutPlanifieDesc(UUID sinistreId);
}
