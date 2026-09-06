package com.logiflow.tms.referential.infrastructure.persistence.repository;

import com.logiflow.tms.referential.infrastructure.persistence.entity.MarchandiseEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarchandiseJpaRepository extends JpaRepository<MarchandiseEntity, UUID> {

  Optional<MarchandiseEntity> findByCode(String code);

  boolean existsByCode(String code);

  Page<MarchandiseEntity> findByLibelleContainingIgnoreCaseOrCodeContainingIgnoreCase(
      String libelle, String code, Pageable pageable);
}
