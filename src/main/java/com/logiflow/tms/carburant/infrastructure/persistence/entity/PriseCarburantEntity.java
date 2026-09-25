package com.logiflow.tms.carburant.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "prise_carburant", schema = "carburant")
public class PriseCarburantEntity extends BaseEntity {

  @Column(name = "voyage_id", nullable = false)
  private UUID voyageId;

  @Column(name = "vehicule_id")
  private UUID vehiculeId;

  @Column(name = "remorque_id")
  private UUID remorqueId;

  @Column(name = "station_id", nullable = false)
  private UUID stationId;

  @Column(name = "type_carburant", nullable = false, length = 20)
  private String typeCarburant;

  @Column(name = "litrage", nullable = false)
  private double litrage;

  @Column(name = "montant_ttc", nullable = false, precision = 12, scale = 2)
  private BigDecimal montantTtc;

  @Column(name = "date_prise", nullable = false)
  private Instant datePrise;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  protected PriseCarburantEntity() {}

  @Builder
  public PriseCarburantEntity(
      UUID id,
      UUID voyageId,
      UUID vehiculeId,
      UUID remorqueId,
      UUID stationId,
      String typeCarburant,
      double litrage,
      BigDecimal montantTtc,
      Instant datePrise,
      String statut) {
    definirId(id);
    this.voyageId = voyageId;
    this.vehiculeId = vehiculeId;
    this.remorqueId = remorqueId;
    this.stationId = stationId;
    this.typeCarburant = typeCarburant;
    this.litrage = litrage;
    this.montantTtc = montantTtc;
    this.datePrise = datePrise;
    this.statut = statut;
  }

  /** Met à jour l'état métier mutable sans remplacer l'identité JPA. */
  public void ecraserEtatMetier(
      UUID stationId, String typeCarburant, double litrage, BigDecimal montantTtc, String statut) {
    this.stationId = stationId;
    this.typeCarburant = typeCarburant;
    this.litrage = litrage;
    this.montantTtc = montantTtc;
    this.statut = statut;
  }
}
