package com.logiflow.tms.iam.infrastructure.persistence.repository;

import com.logiflow.tms.iam.infrastructure.persistence.entity.UtilisateurEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurJpaRepository extends JpaRepository<UtilisateurEntity, UUID> {

  Optional<UtilisateurEntity> findByLogin(String login);

  boolean existsByLogin(String login);

  Page<UtilisateurEntity> findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCase(
      String login, String email, Pageable pageable);
}
