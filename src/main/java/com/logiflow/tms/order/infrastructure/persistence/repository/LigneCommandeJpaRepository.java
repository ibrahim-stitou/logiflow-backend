package com.logiflow.tms.order.infrastructure.persistence.repository;

import com.logiflow.tms.order.infrastructure.persistence.entity.LigneCommandeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneCommandeJpaRepository extends JpaRepository<LigneCommandeEntity, UUID> {

  List<LigneCommandeEntity> findByCommandeId(UUID commandeId);

  void deleteByCommandeId(UUID commandeId);
}
