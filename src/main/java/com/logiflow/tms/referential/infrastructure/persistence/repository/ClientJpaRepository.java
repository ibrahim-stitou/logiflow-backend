package com.logiflow.tms.referential.infrastructure.persistence.repository;

import com.logiflow.tms.referential.infrastructure.persistence.entity.ClientEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {

  Optional<ClientEntity> findByCode(String code);

  boolean existsByCode(String code);

  Page<ClientEntity> findByRaisonSocialeContainingIgnoreCaseOrCodeContainingIgnoreCase(
      String raisonSociale, String code, Pageable pageable);
}
