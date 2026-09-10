package com.logiflow.tms.dossier.infrastructure.persistence.repository;

import com.logiflow.tms.dossier.infrastructure.persistence.entity.LigneMarchandiseEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneMarchandiseJpaRepository extends JpaRepository<LigneMarchandiseEntity, UUID> {

  List<LigneMarchandiseEntity> findByDossierId(UUID dossierId);

  void deleteByDossierId(UUID dossierId);
}
