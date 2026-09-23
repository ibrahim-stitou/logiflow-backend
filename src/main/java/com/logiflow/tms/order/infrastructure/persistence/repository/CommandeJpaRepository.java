package com.logiflow.tms.order.infrastructure.persistence.repository;

import com.logiflow.tms.order.infrastructure.persistence.entity.CommandeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommandeJpaRepository extends JpaRepository<CommandeEntity, UUID> {

  Optional<CommandeEntity> findByReference(String reference);

  Page<CommandeEntity> findByReferenceContainingIgnoreCase(String reference, Pageable pageable);

  @Query(
      """
      SELECT e FROM CommandeEntity e
      WHERE (:statut IS NULL OR e.statut = :statut)
        AND (:q = '' OR LOWER(e.reference) LIKE LOWER(CONCAT('%', :q, '%')))
      ORDER BY e.updatedAt DESC
      """)
  Page<CommandeEntity> rechercherParStatut(
      @Param("q") String texteRecherche, @Param("statut") String statut, Pageable pageable);
}
