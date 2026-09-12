package com.logiflow.tms.dossier.infrastructure.persistence.repository;

import com.logiflow.tms.dossier.infrastructure.persistence.entity.DossierEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DossierJpaRepository extends JpaRepository<DossierEntity, UUID> {

  Optional<DossierEntity> findByReference(String reference);

  List<DossierEntity> findByCommandeId(UUID commandeId);

  Page<DossierEntity> findByReferenceContainingIgnoreCase(String reference, Pageable pageable);
}
