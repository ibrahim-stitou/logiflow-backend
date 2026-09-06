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
 * Entité JPA du véhicule, isolée du modèle de domaine {@link
 * com.logiflow.tms.fleet.domain.model.Vehicule}. Type, énergie, carrosserie et statut sont
 * persistés en texte brut (traduits par le mapper) pour ne faire dépendre l'entité d'aucun type de
 * domaine.
 */
@Getter
@Entity
@Table(
    name = "vehicule",
    schema = "fleet",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_vehicule_immat",
            columnNames = {"immatriculation"}))
public class VehiculeEntity extends BaseEntity {

  @Column(name = "immatriculation", nullable = false, length = 20)
  private String immatriculation;

  @Column(name = "type", nullable = false, length = 20)
  private String type;

  @Column(name = "numero_parc", length = 50)
  private String numeroParc;

  @Column(name = "vin", length = 50)
  private String vin;

  @Column(name = "marque", length = 50)
  private String marque;

  @Column(name = "modele", length = 100)
  private String modele;

  @Column(name = "annee_mise_en_circulation")
  private Integer anneeMiseEnCirculation;

  @Column(name = "energie", length = 30)
  private String energie;

  @Column(name = "ptac_kg", nullable = false)
  private double ptacKg;

  @Column(name = "poids_vide_kg")
  private Double poidsVideKg;

  @Column(name = "charge_utile_kg", nullable = false)
  private double chargeUtileKg;

  @Column(name = "longueur_m")
  private Double longueurM;

  @Column(name = "largeur_m")
  private Double largeurM;

  @Column(name = "hauteur_m")
  private Double hauteurM;

  @Column(name = "volume_utile_m3")
  private Double volumeUtileM3;

  @Column(name = "nb_positions_palettes")
  private Integer nbPositionsPalettes;

  @Column(name = "type_carrosserie", length = 30)
  private String typeCarrosserie;

  @Column(name = "groupe_froid", nullable = false)
  private boolean groupeFroid;

  @Column(name = "temperature_min")
  private Double temperatureMin;

  @Column(name = "temperature_max")
  private Double temperatureMax;

  @Column(name = "kilometrage", nullable = false)
  private int kilometrage;

  @Column(name = "heures_moteur", nullable = false)
  private int heuresMoteur;

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

  @Column(name = "heures_moteur_sortie")
  private Integer heuresMoteurSortie;

  protected VehiculeEntity() {}

  @Builder
  public VehiculeEntity(
      UUID id,
      String immatriculation,
      String type,
      String numeroParc,
      String vin,
      String marque,
      String modele,
      Integer anneeMiseEnCirculation,
      String energie,
      double ptacKg,
      Double poidsVideKg,
      double chargeUtileKg,
      Double longueurM,
      Double largeurM,
      Double hauteurM,
      Double volumeUtileM3,
      Integer nbPositionsPalettes,
      String typeCarrosserie,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      int kilometrage,
      int heuresMoteur,
      String statut,
      LocalDate datePremiereMiseCirculation,
      LocalDate dateAcquisition,
      LocalDate dateMiseEnService,
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresMoteurSortie) {
    definirId(id);
    this.immatriculation = immatriculation;
    this.type = type;
    this.numeroParc = numeroParc;
    this.vin = vin;
    this.marque = marque;
    this.modele = modele;
    this.anneeMiseEnCirculation = anneeMiseEnCirculation;
    this.energie = energie;
    this.ptacKg = ptacKg;
    this.poidsVideKg = poidsVideKg;
    this.chargeUtileKg = chargeUtileKg;
    this.longueurM = longueurM;
    this.largeurM = largeurM;
    this.hauteurM = hauteurM;
    this.volumeUtileM3 = volumeUtileM3;
    this.nbPositionsPalettes = nbPositionsPalettes;
    this.typeCarrosserie = typeCarrosserie;
    this.groupeFroid = groupeFroid;
    this.temperatureMin = temperatureMin;
    this.temperatureMax = temperatureMax;
    this.kilometrage = kilometrage;
    this.heuresMoteur = heuresMoteur;
    this.statut = statut;
    this.datePremiereMiseCirculation = datePremiereMiseCirculation;
    this.dateAcquisition = dateAcquisition;
    this.dateMiseEnService = dateMiseEnService;
    this.dateSortie = dateSortie;
    this.motifSortie = motifSortie;
    this.kilometrageSortie = kilometrageSortie;
    this.heuresMoteurSortie = heuresMoteurSortie;
  }
}
