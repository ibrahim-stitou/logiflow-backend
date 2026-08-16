package com.logiflow.tms.driver.infrastructure.persistence.repository;

import com.logiflow.tms.driver.infrastructure.persistence.entity.ChauffeurEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChauffeurJpaRepository extends JpaRepository<ChauffeurEntity, UUID> {

  Optional<ChauffeurEntity> findByMatricule(String matricule);

  boolean existsByMatricule(String matricule);

  Page<ChauffeurEntity> findByNomCompletContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
      String nomComplet, String matricule, Pageable pageable);
}
