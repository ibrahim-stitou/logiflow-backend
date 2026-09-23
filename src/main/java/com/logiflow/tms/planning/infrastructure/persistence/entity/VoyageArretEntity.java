package com.logiflow.tms.planning.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA d'un arrêt voyage, isolée du modèle de domaine {@link
 * com.logiflow.tms.planning.domain.model.ArretVoyage}.
 */
@Getter
@Entity
@Table(
    name = "voyage_arret",
    schema = "planning",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_voyage_arret_sequence",
            columnNames = {"voyage_id", "indice_sequence"}))
public class VoyageArretEntity extends BaseEntity {

  @Column(name = "voyage_id", nullable = false)
  private UUID voyageId;

  @Column(name = "indice_sequence", nullable = false)
  private int indiceSequence;

  @Column(name = "libelle", nullable = false)
  private String libelle;

  @Column(name = "latitude", nullable = false)
  private double latitude;

  @Column(name = "longitude", nullable = false)
  private double longitude;

  @Column(name = "site_id")
  private UUID siteId;

  @Column(name = "est_original", nullable = false)
  private boolean estOriginal;

  protected VoyageArretEntity() {}

  public void mettreAJourIndiceSequence(int indiceSequence) {
    this.indiceSequence = indiceSequence;
  }

  @Builder
  public VoyageArretEntity(
      UUID id,
      UUID voyageId,
      int indiceSequence,
      String libelle,
      double latitude,
      double longitude,
      UUID siteId,
      boolean estOriginal) {
    definirId(id);
    this.voyageId = voyageId;
    this.indiceSequence = indiceSequence;
    this.libelle = libelle;
    this.latitude = latitude;
    this.longitude = longitude;
    this.siteId = siteId;
    this.estOriginal = estOriginal;
  }
}
