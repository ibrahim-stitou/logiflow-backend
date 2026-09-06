package com.logiflow.tms.fleet.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA de la remorque, isolée du modèle de domaine {@link
 * com.logiflow.tms.fleet.domain.model.Remorque}.
 */
@Getter
@Entity
@Table(
    name = "remorque",
    schema = "fleet",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_remorque_immat",
            columnNames = {"immatriculation"}))
public class RemorqueEntity extends BaseEntity {

  @Column(name = "immatriculation", nullable = false, length = 20)
  private String immatriculation;

  @Column(name = "carrosserie", nullable = false, length = 20)
  private String carrosserie;

  @Column(name = "volume_utile_m3", nullable = false)
  private double volumeUtileM3;

  @Column(name = "nb_positions_palettes", nullable = false)
  private int nbPositionsPalettes;

  @Column(name = "charge_utile_kg", nullable = false)
  private double chargeUtileKg;

  @Column(name = "groupe_froid", nullable = false)
  private boolean groupeFroid;

  @Column(name = "temperature_min")
  private Double temperatureMin;

  @Column(name = "temperature_max")
  private Double temperatureMax;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  protected RemorqueEntity() {}

  @Builder
  public RemorqueEntity(
      UUID id,
      String immatriculation,
      String carrosserie,
      double volumeUtileM3,
      int nbPositionsPalettes,
      double chargeUtileKg,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      String statut) {
    definirId(id);
    this.immatriculation = immatriculation;
    this.carrosserie = carrosserie;
    this.volumeUtileM3 = volumeUtileM3;
    this.nbPositionsPalettes = nbPositionsPalettes;
    this.chargeUtileKg = chargeUtileKg;
    this.groupeFroid = groupeFroid;
    this.temperatureMin = temperatureMin;
    this.temperatureMax = temperatureMax;
    this.statut = statut;
  }
}
