package com.logiflow.tms.driver.infrastructure.persistence.repository;

import com.logiflow.tms.driver.infrastructure.persistence.entity.ChauffeurEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChauffeurJpaRepository extends JpaRepository<ChauffeurEntity, UUID> {

  Optional<ChauffeurEntity> findByMatricule(String matricule);

  boolean existsByMatricule(String matricule);

  Page<ChauffeurEntity>
      findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
          String nom, String prenom, String matricule, Pageable pageable);

  @Query(
      """
      SELECT e FROM ChauffeurEntity e
      WHERE (:disponibilite IS NULL OR e.disponibilite = :disponibilite)
        AND (:q = '' OR LOWER(e.nom) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.matricule) LIKE LOWER(CONCAT('%', :q, '%')))
      ORDER BY e.updatedAt DESC
      """)
  Page<ChauffeurEntity> rechercherParDisponibilite(
      @Param("q") String texteRecherche,
      @Param("disponibilite") String disponibilite,
      Pageable pageable);

  @Query(
      """
      SELECT e FROM ChauffeurEntity e
      WHERE (:statut IS NULL OR e.statut = :statut)
        AND (:disponibilite IS NULL OR e.disponibilite = :disponibilite)
        AND (:q = '' OR LOWER(e.nom) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(e.matricule) LIKE LOWER(CONCAT('%', :q, '%')))
      ORDER BY e.nom ASC, e.prenom ASC
      """)
  Page<ChauffeurEntity> rechercherFiltre(
      @Param("q") String texteRecherche,
      @Param("statut") String statut,
      @Param("disponibilite") String disponibilite,
      Pageable pageable);
}
