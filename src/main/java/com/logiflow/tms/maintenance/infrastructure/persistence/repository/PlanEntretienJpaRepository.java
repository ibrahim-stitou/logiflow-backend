package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.PlanEntretienEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanEntretienJpaRepository extends JpaRepository<PlanEntretienEntity, UUID> {

  List<PlanEntretienEntity> findByVehiculeId(UUID vehiculeId);
}
