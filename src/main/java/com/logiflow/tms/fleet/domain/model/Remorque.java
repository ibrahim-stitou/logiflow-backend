package com.logiflow.tms.fleet.domain.model;

import com.logiflow.tms.shared.domain.vo.Capacite;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import com.logiflow.tms.shared.domain.vo.Poids;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Remorque tractée, porteuse de la capacité de chargement effective d'un voyage.
 *
 * <p>Les documents administratifs et photos de la remorque ne font pas partie de cet agrégat : ils
 * sont gérés par le module {@code document} (relation polymorphe sur cet identifiant).
 */
public final class Remorque {

  private final UUID id;
  private final Immatriculation immatriculation;
  private final TypeRemorque type;
  private final TypeCarrosserie carrosserie;
  private final String numeroParc;
  private final String vin;
  private final String marque;
  private final String modele;
  private final Integer anneeFabrication;
  private final Poids poidsVide;
  private Capacite capaciteUtile;
  private final Double longueurM;
  private final Double largeurM;
  private final Double hauteurM;
  private final boolean groupeFroid;
  private final Double temperatureMin;
  private final Double temperatureMax;
  private int kilometrage;
  private int heuresGroupeFroid;
  private StatutVehicule statut;
  private final LocalDate datePremiereMiseCirculation;
  private final LocalDate dateAcquisition;
  private final LocalDate dateMiseEnService;
  private LocalDate dateSortie;
  private String motifSortie;
  private Integer kilometrageSortie;
  private Integer heuresGroupeFroidSortie;

  private Remorque(
      UUID id,
      Immatriculation immatriculation,
      TypeRemorque type,
      TypeCarrosserie carrosserie,
      String numeroParc,
      String vin,
      String marque,
      String modele,
      Integer anneeFabrication,
      Poids poidsVide,
      Capacite capaciteUtile,
      Double longueurM,
      Double largeurM,
      Double hauteurM,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      int kilometrage,
      int heuresGroupeFroid,
      StatutVehicule statut,
      LocalDate datePremiereMiseCirculation,
      LocalDate dateAcquisition,
      LocalDate dateMiseEnService,
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresGroupeFroidSortie) {
    this.id = Objects.requireNonNull(id, "L'identifiant de la remorque est obligatoire");
    this.immatriculation =
        Objects.requireNonNull(immatriculation, "L'immatriculation est obligatoire");
    this.type = type;
    this.carrosserie = Objects.requireNonNull(carrosserie, "La carrosserie est obligatoire");
    this.numeroParc = numeroParc;
    this.vin = vin;
    this.marque = marque;
    this.modele = modele;
    this.anneeFabrication = anneeFabrication;
    this.poidsVide = poidsVide;
    this.capaciteUtile = Objects.requireNonNull(capaciteUtile, "La capacité utile est obligatoire");
    this.longueurM = longueurM;
    this.largeurM = largeurM;
    this.hauteurM = hauteurM;
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
    if (heuresGroupeFroid < 0) {
      throw new IllegalArgumentException("Les heures groupe froid ne peuvent pas être négatives");
    }
    this.kilometrage = kilometrage;
    this.heuresGroupeFroid = heuresGroupeFroid;
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
    this.datePremiereMiseCirculation = datePremiereMiseCirculation;
    this.dateAcquisition = dateAcquisition;
    this.dateMiseEnService = dateMiseEnService;
    this.dateSortie = dateSortie;
    this.motifSortie = motifSortie;
    this.kilometrageSortie = kilometrageSortie;
    this.heuresGroupeFroidSortie = heuresGroupeFroidSortie;
  }

  /** Création simplifiée, sans les attributs descriptifs optionnels. */
  public static Remorque creer(
      UUID id,
      Immatriculation immatriculation,
      TypeCarrosserie carrosserie,
      Capacite capaciteUtile,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax) {
    return creer(
        id,
        immatriculation,
        null,
        carrosserie,
        null,
        null,
        null,
        null,
        null,
        null,
        capaciteUtile,
        null,
        null,
        null,
        groupeFroid,
        temperatureMin,
        temperatureMax,
        null,
        null,
        null);
  }

  public static Remorque creer(
      UUID id,
      Immatriculation immatriculation,
      TypeRemorque type,
      TypeCarrosserie carrosserie,
      String numeroParc,
      String vin,
      String marque,
      String modele,
      Integer anneeFabrication,
      Poids poidsVide,
      Capacite capaciteUtile,
      Double longueurM,
      Double largeurM,
      Double hauteurM,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      LocalDate datePremiereMiseCirculation,
      LocalDate dateAcquisition,
      LocalDate dateMiseEnService) {
    return new Remorque(
        id,
        immatriculation,
        type,
        carrosserie,
        numeroParc,
        vin,
        marque,
        modele,
        anneeFabrication,
        poidsVide,
        capaciteUtile,
        longueurM,
        largeurM,
        hauteurM,
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

  public static Remorque reconstituer(
      UUID id,
      Immatriculation immatriculation,
      TypeRemorque type,
      TypeCarrosserie carrosserie,
      String numeroParc,
      String vin,
      String marque,
      String modele,
      Integer anneeFabrication,
      Poids poidsVide,
      Capacite capaciteUtile,
      Double longueurM,
      Double largeurM,
      Double hauteurM,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      int kilometrage,
      int heuresGroupeFroid,
      StatutVehicule statut,
      LocalDate datePremiereMiseCirculation,
      LocalDate dateAcquisition,
      LocalDate dateMiseEnService,
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresGroupeFroidSortie) {
    return new Remorque(
        id,
        immatriculation,
        type,
        carrosserie,
        numeroParc,
        vin,
        marque,
        modele,
        anneeFabrication,
        poidsVide,
        capaciteUtile,
        longueurM,
        largeurM,
        hauteurM,
        groupeFroid,
        temperatureMin,
        temperatureMax,
        kilometrage,
        heuresGroupeFroid,
        statut,
        datePremiereMiseCirculation,
        dateAcquisition,
        dateMiseEnService,
        dateSortie,
        motifSortie,
        kilometrageSortie,
        heuresGroupeFroidSortie);
  }

  public void changerStatut(StatutVehicule nouveauStatut) {
    this.statut = Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
  }

  public void mettreAJourCapacite(Capacite capaciteUtile) {
    this.capaciteUtile = Objects.requireNonNull(capaciteUtile, "La capacité utile est obligatoire");
  }

  public void relever(int nouveauKilometrage, int nouvellesHeuresGroupeFroid) {
    if (nouveauKilometrage < this.kilometrage) {
      throw new IllegalArgumentException("Le kilométrage relevé ne peut pas régresser");
    }
    if (nouvellesHeuresGroupeFroid < this.heuresGroupeFroid) {
      throw new IllegalArgumentException(
          "Les heures groupe froid relevées ne peuvent pas régresser");
    }
    this.kilometrage = nouveauKilometrage;
    this.heuresGroupeFroid = nouvellesHeuresGroupeFroid;
  }

  /** Sort définitivement la remorque du parc (réforme, vente, destruction...). */
  public void sortir(
      LocalDate dateSortie,
      String motifSortie,
      Integer kilometrageSortie,
      Integer heuresGroupeFroidSortie) {
    this.dateSortie = Objects.requireNonNull(dateSortie, "La date de sortie est obligatoire");
    this.motifSortie = motifSortie;
    this.kilometrageSortie = kilometrageSortie;
    this.heuresGroupeFroidSortie = heuresGroupeFroidSortie;
    this.statut = StatutVehicule.HORS_SERVICE;
  }

  /** Une remorque frigorifique est compatible si la température requise entre dans sa plage. */
  public boolean compatibleTemperature(double temperatureRequise) {
    if (!groupeFroid) {
      return false;
    }
    return temperatureRequise >= temperatureMin && temperatureRequise <= temperatureMax;
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

  public TypeRemorque type() {
    return type;
  }

  public TypeCarrosserie carrosserie() {
    return carrosserie;
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

  public Integer anneeFabrication() {
    return anneeFabrication;
  }

  public Poids poidsVide() {
    return poidsVide;
  }

  public Capacite capaciteUtile() {
    return capaciteUtile;
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

  public int heuresGroupeFroid() {
    return heuresGroupeFroid;
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

  public Integer heuresGroupeFroidSortie() {
    return heuresGroupeFroidSortie;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Remorque remorque)) {
      return false;
    }
    return id.equals(remorque.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
