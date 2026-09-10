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
 * Entité JPA du véhicule, isolée du modèle de domaine {@link
 * com.logiflow.tms.fleet.domain.model.Vehicule}. Type et statut sont persistés en texte brut
 * (traduits par le mapper) pour ne faire dépendre l'entité d'aucun type de domaine.
 */
@Getter
@Entity
@Table(
    name = "vehicule",
    schema = "fleet",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_vehicule_tenant_immat",
            columnNames = {"tenant_id", "immatriculation"}))
public class VehiculeEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "immatriculation", nullable = false, length = 20)
  private String immatriculation;

  @Column(name = "type", nullable = false, length = 20)
  private String type;

  @Column(name = "ptac_kg", nullable = false)
  private double ptacKg;

  @Column(name = "charge_utile_kg", nullable = false)
  private double chargeUtileKg;

  @Column(name = "kilometrage", nullable = false)
  private int kilometrage;

  @Column(name = "heures_moteur", nullable = false)
  private int heuresMoteur;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "documents_json", columnDefinition = "text")
  private String documentsJson;

  protected VehiculeEntity() {}

  @Builder
  public VehiculeEntity(
      UUID id,
      UUID tenantId,
      String immatriculation,
      String type,
      double ptacKg,
      double chargeUtileKg,
      int kilometrage,
      int heuresMoteur,
      String statut,
      String documentsJson) {
    definirId(id);
    this.tenantId = tenantId;
    this.immatriculation = immatriculation;
    this.type = type;
    this.ptacKg = ptacKg;
    this.chargeUtileKg = chargeUtileKg;
    this.kilometrage = kilometrage;
    this.heuresMoteur = heuresMoteur;
    this.statut = statut;
    this.documentsJson = documentsJson;
  }

  public void ecraserEtatMetier(
      String immatriculation,
      String type,
      double ptacKg,
      double chargeUtileKg,
      int kilometrage,
      int heuresMoteur,
      String statut,
      String documentsJson) {
    this.immatriculation = immatriculation;
    this.type = type;
    this.ptacKg = ptacKg;
    this.chargeUtileKg = chargeUtileKg;
    this.kilometrage = kilometrage;
    this.heuresMoteur = heuresMoteur;
    this.statut = statut;
    this.documentsJson = documentsJson;
  }
}
