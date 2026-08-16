package com.logiflow.tms.maintenance.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "ordre_travail", schema = "maintenance")
public class OrdreTravailEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "vehicule_id", nullable = false)
  private UUID vehiculeId;

  @Column(name = "type_intervention", nullable = false, length = 30)
  private String typeIntervention;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "date_planifiee", nullable = false)
  private LocalDateTime datePlanifiee;

  @Column(name = "duree_reelle_min", nullable = false)
  private int dureeReelleMin;

  @Column(name = "cout_montant", nullable = false, precision = 12, scale = 2)
  private BigDecimal coutMontant;

  @Column(name = "cout_devise", nullable = false, length = 3)
  private String coutDevise;

  protected OrdreTravailEntity() {}

  @Builder
  public OrdreTravailEntity(
      UUID id,
      UUID tenantId,
      UUID vehiculeId,
      String typeIntervention,
      String statut,
      LocalDateTime datePlanifiee,
      int dureeReelleMin,
      BigDecimal coutMontant,
      String coutDevise) {
    definirId(id);
    this.tenantId = tenantId;
    this.vehiculeId = vehiculeId;
    this.typeIntervention = typeIntervention;
    this.statut = statut;
    this.datePlanifiee = datePlanifiee;
    this.dureeReelleMin = dureeReelleMin;
    this.coutMontant = coutMontant;
    this.coutDevise = coutDevise;
  }
}
