package com.logiflow.tms.referential.infrastructure.persistence.repository;

import com.logiflow.tms.referential.infrastructure.persistence.entity.SiteEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteJpaRepository extends JpaRepository<SiteEntity, UUID> {

  Optional<SiteEntity> findByCode(String code);

  boolean existsByCode(String code);

  Page<SiteEntity> findByLibelleContainingIgnoreCaseOrCodeContainingIgnoreCase(
      String libelle, String code, Pageable pageable);
}
