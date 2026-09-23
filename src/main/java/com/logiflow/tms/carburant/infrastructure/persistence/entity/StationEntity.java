package com.logiflow.tms.carburant.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "station",
    schema = "carburant",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_station_code",
            columnNames = {"code"}))
public class StationEntity extends BaseEntity {

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "libelle", nullable = false, length = 255)
  private String libelle;

  @Column(name = "adresse", length = 500)
  private String adresse;

  @Column(name = "actif", nullable = false)
  private boolean actif;

  protected StationEntity() {}

  @Builder
  public StationEntity(UUID id, String code, String libelle, String adresse, boolean actif) {
    definirId(id);
    this.code = code;
    this.libelle = libelle;
    this.adresse = adresse;
    this.actif = actif;
  }

  /** Met à jour l'état métier mutable sans remplacer l'identité JPA. */
  public void ecraserEtatMetier(String libelle, String adresse, boolean actif) {
    this.libelle = libelle;
    this.adresse = adresse;
    this.actif = actif;
  }
}
