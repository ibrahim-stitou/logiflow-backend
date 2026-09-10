package com.logiflow.tms.referential.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA du client, isolée du modèle de domaine {@link
 * com.logiflow.tms.referential.domain.model.Client}.
 */
@Getter
@Entity
@Table(
    name = "client",
    schema = "referential",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_client_code",
            columnNames = {"code"}))
public class ClientEntity extends BaseEntity {

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "raison_sociale", nullable = false, length = 255)
  private String raisonSociale;

  @Column(name = "actif", nullable = false)
  private boolean actif;

  protected ClientEntity() {}

  @Builder
  public ClientEntity(UUID id, String code, String raisonSociale, boolean actif) {
    definirId(id);
    this.code = code;
    this.raisonSociale = raisonSociale;
    this.actif = actif;
  }
}
