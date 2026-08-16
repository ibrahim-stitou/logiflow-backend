package com.logiflow.tms.order.infrastructure.persistence.repository;

import com.logiflow.tms.order.infrastructure.persistence.entity.CommandeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeJpaRepository extends JpaRepository<CommandeEntity, UUID> {

  Optional<CommandeEntity> findByReference(String reference);
}
