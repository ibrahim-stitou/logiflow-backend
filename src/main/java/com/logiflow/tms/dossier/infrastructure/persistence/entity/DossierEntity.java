package com.logiflow.tms.dossier.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA du dossier de transport, isolée du modèle de domaine {@link
 * com.logiflow.tms.dossier.domain.model.DossierTransport}.
 */
@Getter
@Entity
@Table(
    name = "dossier_transport",
    schema = "dossier",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_dossier_reference",
            columnNames = {"reference"}))
public class DossierEntity extends BaseEntity {

  @Column(name = "reference", nullable = false, length = 20)
  private String reference;

  @Column(name = "commande_id", nullable = false)
  private UUID commandeId;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "type_transport", nullable = false, length = 20)
  private String typeTransport;

  @Column(name = "groupable", nullable = false)
  private boolean groupable;

  @Column(name = "poids_brut_kg", nullable = false)
  private double poidsBrutKg;

  @Column(name = "volume_m3", nullable = false)
  private double volumeM3;

  @Column(name = "nb_palettes", nullable = false)
  private int nbPalettes;

  @Column(name = "famille_marchandise", nullable = false, length = 100)
  private String familleMarchandise;

  @Column(name = "carrosserie_requise", length = 20)
  private String carrosserieRequise;

  @Column(name = "temperature_requise")
  private Double temperatureRequise;

  @Column(name = "segments_json", nullable = false, columnDefinition = "text")
  private String segmentsJson;

  @Column(name = "documents_json", columnDefinition = "text")
  private String documentsJson;

  @Column(name = "arret_chargement_id")
  private UUID arretChargementId;

  @Column(name = "arret_dechargement_id")
  private UUID arretDechargementId;

  protected DossierEntity() {}

  @Builder
  public DossierEntity(
      UUID id,
      String reference,
      UUID commandeId,
      String statut,
      String typeTransport,
      boolean groupable,
      double poidsBrutKg,
      double volumeM3,
      int nbPalettes,
      String familleMarchandise,
      String carrosserieRequise,
      Double temperatureRequise,
      String segmentsJson,
      String documentsJson,
      UUID arretChargementId,
      UUID arretDechargementId) {
    definirId(id);
    this.reference = reference;
    this.commandeId = commandeId;
    this.statut = statut;
    this.typeTransport = typeTransport;
    this.groupable = groupable;
    this.poidsBrutKg = poidsBrutKg;
    this.volumeM3 = volumeM3;
    this.nbPalettes = nbPalettes;
    this.familleMarchandise = familleMarchandise;
    this.carrosserieRequise = carrosserieRequise;
    this.temperatureRequise = temperatureRequise;
    this.segmentsJson = segmentsJson;
    this.documentsJson = documentsJson;
    this.arretChargementId = arretChargementId;
    this.arretDechargementId = arretDechargementId;
  }

  /** Met à jour l'état métier sans toucher à l'identifiant ni au verrou optimiste. */
  public void ecraserEtatMetier(
      String statut,
      String typeTransport,
      boolean groupable,
      double poidsBrutKg,
      double volumeM3,
      int nbPalettes,
      String familleMarchandise,
      String carrosserieRequise,
      Double temperatureRequise,
      String segmentsJson,
      String documentsJson,
      UUID arretChargementId,
      UUID arretDechargementId) {
    this.statut = statut;
    this.typeTransport = typeTransport;
    this.groupable = groupable;
    this.poidsBrutKg = poidsBrutKg;
    this.volumeM3 = volumeM3;
    this.nbPalettes = nbPalettes;
    this.familleMarchandise = familleMarchandise;
    this.carrosserieRequise = carrosserieRequise;
    this.temperatureRequise = temperatureRequise;
    this.segmentsJson = segmentsJson;
    this.documentsJson = documentsJson;
    this.arretChargementId = arretChargementId;
    this.arretDechargementId = arretDechargementId;
  }
}
