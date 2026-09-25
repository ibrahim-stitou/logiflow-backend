package com.logiflow.tms.dossier.infrastructure.persistence.repository;

import com.logiflow.tms.dossier.infrastructure.persistence.entity.DossierEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DossierJpaRepository extends JpaRepository<DossierEntity, UUID> {

  Optional<DossierEntity> findByReference(String reference);

  List<DossierEntity> findByCommandeId(UUID commandeId);

  List<DossierEntity> findByStatut(String statut);

  Page<DossierEntity> findByReferenceContainingIgnoreCase(String reference, Pageable pageable);

  @Query(
      """
      SELECT e FROM DossierEntity e
      WHERE (:statut IS NULL OR e.statut = :statut)
        AND (:q = '' OR LOWER(e.reference) LIKE LOWER(CONCAT('%', :q, '%')))
      ORDER BY e.updatedAt DESC
      """)
  Page<DossierEntity> rechercherParStatut(
      @Param("q") String texteRecherche, @Param("statut") String statut, Pageable pageable);
}
