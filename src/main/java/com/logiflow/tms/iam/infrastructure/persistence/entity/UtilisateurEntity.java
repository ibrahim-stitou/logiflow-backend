package com.logiflow.tms.iam.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA de l'utilisateur, isolée du modèle de domaine {@link
 * com.logiflow.tms.iam.domain.model.Utilisateur}.
 */
@Getter
@Entity
@Table(
    name = "utilisateur",
    schema = "iam",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_utilisateur_tenant_login",
            columnNames = {"tenant_id", "login"}))
public class UtilisateurEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "login", nullable = false, length = 100)
  private String login;

  @Column(name = "email", nullable = false, length = 255)
  private String email;

  @Column(name = "roles_json", nullable = false, columnDefinition = "text")
  private String rolesJson;

  @Column(name = "actif", nullable = false)
  private boolean actif;

  protected UtilisateurEntity() {}

  @Builder
  public UtilisateurEntity(
      UUID id, UUID tenantId, String login, String email, String rolesJson, boolean actif) {
    definirId(id);
    this.tenantId = tenantId;
    this.login = login;
    this.email = email;
    this.rolesJson = rolesJson;
    this.actif = actif;
  }
}
