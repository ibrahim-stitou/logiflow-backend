package com.logiflow.tms.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Superclasse technique des entités JPA : identifiant applicatif, audit et verrouillage optimiste.
 * Réservée à la couche {@code infrastructure.persistence} : le modèle de domaine ne dépend jamais
 * de cette classe.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Auditable {

  @Id
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @LastModifiedBy
  @Column(name = "updated_by", nullable = false, length = 100)
  private String updatedBy;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @PrePersist
  protected void assignerIdentifiantSiAbsent() {
    if (id == null) {
      id = UUID.randomUUID();
    }
  }

  protected void definirId(UUID id) {
    this.id = id;
  }
}
