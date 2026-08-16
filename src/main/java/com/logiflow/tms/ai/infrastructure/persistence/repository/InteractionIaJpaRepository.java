package com.logiflow.tms.ai.infrastructure.persistence.repository;

import com.logiflow.tms.ai.infrastructure.persistence.entity.InteractionIaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionIaJpaRepository extends JpaRepository<InteractionIaEntity, UUID> {}
