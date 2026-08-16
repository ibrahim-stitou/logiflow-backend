package com.logiflow.tms.maintenance.infrastructure.persistence.repository;

import com.logiflow.tms.maintenance.infrastructure.persistence.entity.OrdreTravailEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdreTravailJpaRepository extends JpaRepository<OrdreTravailEntity, UUID> {

  List<OrdreTravailEntity> findByVehiculeId(UUID vehiculeId);
}
