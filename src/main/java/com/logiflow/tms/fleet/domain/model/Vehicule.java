package com.logiflow.tms.fleet.domain.model;

import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Véhicule motorisé (tracteur, porteur, fourgon). Entité racine du sous-domaine Véhicule du module
 * {@code fleet}.
 *
 * <p>La disponibilité vis-à-vis d'une période donnée dépend des voyages déjà planifiés : cette
 * vérification appartient au module {@code planning}, qui consomme {@link
 * com.logiflow.tms.fleet.api.VehiculeApi#estDisponible(UUID)} pour le statut courant puis croise
 * ses propres données de planning.
 *
 * <p>Les documents administratifs et photos du véhicule ne font pas partie de cet agrégat : ils
 * sont gérés par le module {@code document} (relation polymorphe sur cet identifiant), consultés
 * via {@link com.logiflow.tms.fleet.api.VehiculeApi#documentsValides(UUID, LocalDate)}.
 */
public final class Vehicule {

  private final UUID id;
  private final Immatriculation immatriculation;
  private final TypeVehicule type;
  private final String numeroParc;
  private final String vin;
  private final String marque;
  private final String modele;
  private final Integer anneeMiseEnCirculation;
  private final Energie energie;
  private Poids ptac;
  private final Poids poidsVide;
  private Poids chargeUtile;
  private final Double longueurM;
  private final Double largeurM;
  private final Double hauteurM;
  private final Double volumeUtileM3;
  private final Integer nbPositionsPalettes;
  private final TypeCarrosserie typeCarrosserie;
  private final boolean groupeFroid;
  private final Double temperatureMin;
  private final Double temperatureMax;
  private int kilometrage;
  private int heuresMoteur;
  private StatutVehicule statut;
  private final LocalDate datePremiereMiseCirculation;
  private final LocalDate dateAcquisition;
  private final LocalDate dateMiseEnService;
  private LocalDate dateSortie;
  private String motifSortie;
  private Integer kilometrageSortie;
  private Integer heuresMoteurSortie;

  private Vehicule(
      UUID id,
      Immatriculation immatriculation,
      TypeVehicule type,
      String numeroParc,
      String vin,
      String marque,
      String modele,
      Integer anneeMiseEnCirculation,
      Energie energie,
      Poids ptac,
      Poids poidsVide,
      Poids chargeUtile,
      Double longueurM,
      Double largeurM,
      Double hauteurM,
      Double volumeUtileM3,
      Integer nbPositionsPalettes,
      TypeCarrosserie typeCarrosserie,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      int kilometrage,
      int heuresMoteur,
      StatutVehicule statut,
      LocalDate datePremiereMiseCirculation,
      LocalDate dateAcquisition,
      LocalDate dateMiseEnService,
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresMoteurSortie) {
    this.id = Objects.requireNonNull(id, "L'identifiant du véhicule est obligatoire");
    this.immatriculation =
        Objects.requireNonNull(immatriculation, "L'immatriculation est obligatoire");
    this.type = Objects.requireNonNull(type, "Le type de véhicule est obligatoire");
    this.numeroParc = numeroParc;
    this.vin = vin;
    this.marque = marque;
    this.modele = modele;
    this.anneeMiseEnCirculation = anneeMiseEnCirculation;
    this.energie = energie;
    this.ptac = Objects.requireNonNull(ptac, "Le PTAC est obligatoire");
    this.poidsVide = poidsVide;
    this.chargeUtile = Objects.requireNonNull(chargeUtile, "La charge utile est obligatoire");
    this.longueurM = longueurM;
    this.largeurM = largeurM;
    this.hauteurM = hauteurM;
    this.volumeUtileM3 = volumeUtileM3;
    this.nbPositionsPalettes = nbPositionsPalettes;
    this.typeCarrosserie = typeCarrosserie;
    if (groupeFroid) {
      Objects.requireNonNull(
          temperatureMin, "La température minimale est obligatoire pour un groupe froid");
      Objects.requireNonNull(
          temperatureMax, "La température maximale est obligatoire pour un groupe froid");
      if (temperatureMin > temperatureMax) {
        throw new IllegalArgumentException(
            "La température minimale ne peut pas dépasser la température maximale");
      }
    }
    this.groupeFroid = groupeFroid;
    this.temperatureMin = temperatureMin;
    this.temperatureMax = temperatureMax;
    if (kilometrage < 0) {
      throw new IllegalArgumentException("Le kilométrage ne peut pas être négatif");
    }
    if (heuresMoteur < 0) {
      throw new IllegalArgumentException("Les heures moteur ne peuvent pas être négatives");
    }
    this.kilometrage = kilometrage;
    this.heuresMoteur = heuresMoteur;
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.datePremiereMiseCirculation = datePremiereMiseCirculation;
    this.dateAcquisition = dateAcquisition;
    this.dateMiseEnService = dateMiseEnService;
    this.dateSortie = dateSortie;
    this.motifSortie = motifSortie;
    this.kilometrageSortie = kilometrageSortie;
    this.heuresMoteurSortie = heuresMoteurSortie;
  }

  /**
   * Création simplifiée, sans les attributs descriptifs optionnels (immatriculation SIV en test).
   */
  public static Vehicule creer(
      UUID id, Immatriculation immatriculation, TypeVehicule type, Poids ptac, Poids chargeUtile) {
    return creer(
        id,
        immatriculation,
        type,
        null,
        null,
        null,
        null,
        null,
        null,
        ptac,
        null,
        chargeUtile,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        null,
        null);
  }

  public static Vehicule creer(
      UUID id,
      Immatriculation immatriculation,
      TypeVehicule type,
      String numeroParc,
      String vin,
      String marque,
      String modele,
      Integer anneeMiseEnCirculation,
      Energie energie,
      Poids ptac,
      Poids poidsVide,
      Poids chargeUtile,
      Double longueurM,
      Double largeurM,
      Double hauteurM,
      Double volumeUtileM3,
      Integer nbPositionsPalettes,
      TypeCarrosserie typeCarrosserie,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      LocalDate datePremiereMiseCirculation,
      LocalDate dateAcquisition,
      LocalDate dateMiseEnService) {
    return new Vehicule(
        id,
        immatriculation,
        type,
        numeroParc,
        vin,
        marque,
        modele,
        anneeMiseEnCirculation,
        energie,
        ptac,
        poidsVide,
        chargeUtile,
        longueurM,
        largeurM,
        hauteurM,
        volumeUtileM3,
        nbPositionsPalettes,
        typeCarrosserie,
        groupeFroid,
        temperatureMin,
        temperatureMax,
        0,
        0,
        StatutVehicule.DISPONIBLE,
        datePremiereMiseCirculation,
        dateAcquisition,
        dateMiseEnService,
        null,
        null,
        null,
        null);
  }

  public static Vehicule reconstituer(
      UUID id,
      Immatriculation immatriculation,
      TypeVehicule type,
      String numeroParc,
      String vin,
      String marque,
      String modele,
      Integer anneeMiseEnCirculation,
      Energie energie,
      Poids ptac,
      Poids poidsVide,
      Poids chargeUtile,
      Double longueurM,
      Double largeurM,
      Double hauteurM,
      Double volumeUtileM3,
      Integer nbPositionsPalettes,
      TypeCarrosserie typeCarrosserie,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      int kilometrage,
      int heuresMoteur,
      StatutVehicule statut,
      LocalDate datePremiereMiseCirculation,
      LocalDate dateAcquisition,
      LocalDate dateMiseEnService,
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresMoteurSortie) {
    return new Vehicule(
        id,
        immatriculation,
        type,
        numeroParc,
        vin,
        marque,
        modele,
        anneeMiseEnCirculation,
        energie,
        ptac,
        poidsVide,
        chargeUtile,
        longueurM,
        largeurM,
        hauteurM,
        volumeUtileM3,
        nbPositionsPalettes,
        typeCarrosserie,
        groupeFroid,
        temperatureMin,
        temperatureMax,
        kilometrage,
        heuresMoteur,
        statut,
        datePremiereMiseCirculation,
        dateAcquisition,
        dateMiseEnService,
        dateSortie,
        motifSortie,
        kilometrageSortie,
        heuresMoteurSortie);
  }

  public void relever(int nouveauKilometrage, int nouvellesHeuresMoteur) {
    if (nouveauKilometrage < this.kilometrage) {
      throw new IllegalArgumentException("Le kilométrage relevé ne peut pas régresser");
    }
    if (nouvellesHeuresMoteur < this.heuresMoteur) {
      throw new IllegalArgumentException("Les heures moteur relevées ne peuvent pas régresser");
    }
    this.kilometrage = nouveauKilometrage;
    this.heuresMoteur = nouvellesHeuresMoteur;
  }

  public void changerStatut(StatutVehicule nouveauStatut) {
    this.statut = Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
  }

  /** Sort définitivement le véhicule du parc (réforme, vente, destruction...). */
  public void sortir(
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresMoteurSortie) {
    this.dateSortie = Objects.requireNonNull(dateSortie, "La date de sortie est obligatoire");
    this.motifSortie = motifSortie;
    this.kilometrageSortie = kilometrageSortie;
    this.heuresMoteurSortie = heuresMoteurSortie;
    this.statut = StatutVehicule.HORS_SERVICE;
  }

  public boolean estDisponible() {
    return statut == StatutVehicule.DISPONIBLE;
  }

  public UUID id() {
    return id;
  }

  public Immatriculation immatriculation() {
    return immatriculation;
  }

  public TypeVehicule type() {
    return type;
  }

  public String numeroParc() {
    return numeroParc;
  }

  public String vin() {
    return vin;
  }

  public String marque() {
    return marque;
  }

  public String modele() {
    return modele;
  }

  public Integer anneeMiseEnCirculation() {
    return anneeMiseEnCirculation;
  }

  public Energie energie() {
    return energie;
  }

  public Poids ptac() {
    return ptac;
  }

  public Poids poidsVide() {
    return poidsVide;
  }

  public Poids chargeUtile() {
    return chargeUtile;
  }

  public Double longueurM() {
    return longueurM;
  }

  public Double largeurM() {
    return largeurM;
  }

  public Double hauteurM() {
    return hauteurM;
  }

  public Double volumeUtileM3() {
    return volumeUtileM3;
  }

  public Integer nbPositionsPalettes() {
    return nbPositionsPalettes;
  }

  public TypeCarrosserie typeCarrosserie() {
    return typeCarrosserie;
  }

  public boolean groupeFroid() {
    return groupeFroid;
  }

  public Double temperatureMin() {
    return temperatureMin;
  }

  public Double temperatureMax() {
    return temperatureMax;
  }

  public int kilometrage() {
    return kilometrage;
  }

  public int heuresMoteur() {
    return heuresMoteur;
  }

  public StatutVehicule statut() {
    return statut;
  }

  public LocalDate datePremiereMiseCirculation() {
    return datePremiereMiseCirculation;
  }

  public LocalDate dateAcquisition() {
    return dateAcquisition;
  }

  public LocalDate dateMiseEnService() {
    return dateMiseEnService;
  }

  public LocalDate dateSortie() {
    return dateSortie;
  }

  public String motifSortie() {
    return motifSortie;
  }

  public Integer kilometrageSortie() {
    return kilometrageSortie;
  }

  public Integer heuresMoteurSortie() {
    return heuresMoteurSortie;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Vehicule vehicule)) {
      return false;
    }
    return id.equals(vehicule.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
