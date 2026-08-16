package com.logiflow.tms.maintenance.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "plan_entretien", schema = "maintenance")
public class PlanEntretienEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "vehicule_id", nullable = false)
  private UUID vehiculeId;

  @Column(name = "libelle", nullable = false, length = 255)
  private String libelle;

  @Column(name = "periodicite_km")
  private Integer periodiciteKm;

  @Column(name = "periodicite_mois")
  private Integer periodiciteMois;

  @Column(name = "seuil_alerte_km", nullable = false)
  private int seuilAlerteKm;

  @Column(name = "duree_estimee_min", nullable = false)
  private int dureeEstimeeMin;

  protected PlanEntretienEntity() {}

  @Builder
  public PlanEntretienEntity(
      UUID id,
      UUID tenantId,
      UUID vehiculeId,
      String libelle,
      Integer periodiciteKm,
      Integer periodiciteMois,
      int seuilAlerteKm,
      int dureeEstimeeMin) {
    definirId(id);
    this.tenantId = tenantId;
    this.vehiculeId = vehiculeId;
    this.libelle = libelle;
    this.periodiciteKm = periodiciteKm;
    this.periodiciteMois = periodiciteMois;
    this.seuilAlerteKm = seuilAlerteKm;
    this.dureeEstimeeMin = dureeEstimeeMin;
  }
}
