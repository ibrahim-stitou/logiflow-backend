package com.logiflow.tms.driver.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA du chauffeur, isolée du modèle de domaine {@link
 * com.logiflow.tms.driver.domain.model.Chauffeur}.
 */
@Getter
@Entity
@Table(
    name = "chauffeur",
    schema = "driver",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_chauffeur_tenant_matricule",
            columnNames = {"tenant_id", "matricule"}))
public class ChauffeurEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "matricule", nullable = false, length = 30)
  private String matricule;

  @Column(name = "nom_complet", nullable = false, length = 255)
  private String nomComplet;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "solde_temps_conduite_minutes", nullable = false)
  private long soldeTempsConduiteMinutes;

  @Column(name = "habilitations_json", columnDefinition = "text")
  private String habilitationsJson;

  protected ChauffeurEntity() {}

  @Builder
  public ChauffeurEntity(
      UUID id,
      UUID tenantId,
      String matricule,
      String nomComplet,
      String statut,
      long soldeTempsConduiteMinutes,
      String habilitationsJson) {
    definirId(id);
    this.tenantId = tenantId;
    this.matricule = matricule;
    this.nomComplet = nomComplet;
    this.statut = statut;
    this.soldeTempsConduiteMinutes = soldeTempsConduiteMinutes;
    this.habilitationsJson = habilitationsJson;
  }
}
