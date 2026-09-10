package com.logiflow.tms.fleet.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
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

  @Column(name = "type", length = 30)
  private String type;

  @Column(name = "carrosserie", nullable = false, length = 20)
  private String carrosserie;

  @Column(name = "numero_parc", length = 50)
  private String numeroParc;

  @Column(name = "vin", length = 50)
  private String vin;

  @Column(name = "marque", length = 50)
  private String marque;

  @Column(name = "modele", length = 100)
  private String modele;

  @Column(name = "annee_fabrication")
  private Integer anneeFabrication;

  @Column(name = "poids_vide_kg")
  private Double poidsVideKg;

  @Column(name = "volume_utile_m3", nullable = false)
  private double volumeUtileM3;

  @Column(name = "nb_positions_palettes", nullable = false)
  private int nbPositionsPalettes;

  @Column(name = "charge_utile_kg", nullable = false)
  private double chargeUtileKg;

  @Column(name = "longueur_m")
  private Double longueurM;

  @Column(name = "largeur_m")
  private Double largeurM;

  @Column(name = "hauteur_m")
  private Double hauteurM;

  @Column(name = "groupe_froid", nullable = false)
  private boolean groupeFroid;

  @Column(name = "temperature_min")
  private Double temperatureMin;

  @Column(name = "temperature_max")
  private Double temperatureMax;

  @Column(name = "kilometrage", nullable = false)
  private int kilometrage;

  @Column(name = "heures_groupe_froid", nullable = false)
  private int heuresGroupeFroid;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "date_premiere_mise_circulation")
  private LocalDate datePremiereMiseCirculation;

  @Column(name = "date_acquisition")
  private LocalDate dateAcquisition;

  @Column(name = "date_mise_en_service")
  private LocalDate dateMiseEnService;

  @Column(name = "date_sortie")
  private LocalDate dateSortie;

  @Column(name = "motif_sortie", length = 100)
  private String motifSortie;

  @Column(name = "kilometrage_sortie")
  private Integer kilometrageSortie;

  @Column(name = "heures_groupe_froid_sortie")
  private Integer heuresGroupeFroidSortie;

  protected RemorqueEntity() {}

  @Builder
  public RemorqueEntity(
      UUID id,
      String immatriculation,
      String type,
      String carrosserie,
      String numeroParc,
      String vin,
      String marque,
      String modele,
      Integer anneeFabrication,
      Double poidsVideKg,
      double volumeUtileM3,
      int nbPositionsPalettes,
      double chargeUtileKg,
      Double longueurM,
      Double largeurM,
      Double hauteurM,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      int kilometrage,
      int heuresGroupeFroid,
      String statut,
      LocalDate datePremiereMiseCirculation,
      LocalDate dateAcquisition,
      LocalDate dateMiseEnService,
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresGroupeFroidSortie) {
    definirId(id);
    this.immatriculation = immatriculation;
    this.type = type;
    this.carrosserie = carrosserie;
    this.numeroParc = numeroParc;
    this.vin = vin;
    this.marque = marque;
    this.modele = modele;
    this.anneeFabrication = anneeFabrication;
    this.poidsVideKg = poidsVideKg;
    this.volumeUtileM3 = volumeUtileM3;
    this.nbPositionsPalettes = nbPositionsPalettes;
    this.chargeUtileKg = chargeUtileKg;
    this.longueurM = longueurM;
    this.largeurM = largeurM;
    this.hauteurM = hauteurM;
    this.groupeFroid = groupeFroid;
    this.temperatureMin = temperatureMin;
    this.temperatureMax = temperatureMax;
    this.kilometrage = kilometrage;
    this.heuresGroupeFroid = heuresGroupeFroid;
    this.statut = statut;
    this.datePremiereMiseCirculation = datePremiereMiseCirculation;
    this.dateAcquisition = dateAcquisition;
    this.dateMiseEnService = dateMiseEnService;
    this.dateSortie = dateSortie;
    this.motifSortie = motifSortie;
    this.kilometrageSortie = kilometrageSortie;
    this.heuresGroupeFroidSortie = heuresGroupeFroidSortie;
  }
}
