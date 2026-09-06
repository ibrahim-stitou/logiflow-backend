package com.logiflow.tms.document.infrastructure.persistence.repository;

import com.logiflow.tms.document.infrastructure.persistence.entity.DocumentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentJpaRepository extends JpaRepository<DocumentEntity, UUID> {

  List<DocumentEntity> findByTypeEntiteAndEntiteId(String typeEntite, UUID entiteId);
}
