package com.logiflow.tms.fleet.domain.model;

import com.logiflow.tms.shared.domain.vo.Capacite;
import com.logiflow.tms.shared.domain.vo.Immatriculation;
import java.util.Objects;
import java.util.UUID;

/** Remorque tractée, porteuse de la capacité de chargement effective d'un voyage. */
public final class Remorque {

  private final UUID id;
  private final Immatriculation immatriculation;
  private final TypeCarrosserie carrosserie;
  private Capacite capaciteUtile;
  private final boolean groupeFroid;
  private final Double temperatureMin;
  private final Double temperatureMax;
  private StatutVehicule statut;

  private Remorque(
      UUID id,
      Immatriculation immatriculation,
      TypeCarrosserie carrosserie,
      Capacite capaciteUtile,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      StatutVehicule statut) {
    this.id = Objects.requireNonNull(id, "L'identifiant de la remorque est obligatoire");
    this.immatriculation =
        Objects.requireNonNull(immatriculation, "L'immatriculation est obligatoire");
    this.carrosserie = Objects.requireNonNull(carrosserie, "La carrosserie est obligatoire");
    this.capaciteUtile = Objects.requireNonNull(capaciteUtile, "La capacité utile est obligatoire");
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
    this.statut = Objects.requireNonNull(statut, "Le statut est obligatoire");
  }

  public static Remorque creer(
      UUID id,
      Immatriculation immatriculation,
      TypeCarrosserie carrosserie,
      Capacite capaciteUtile,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax) {
    return new Remorque(
        id,
        immatriculation,
        carrosserie,
        capaciteUtile,
        groupeFroid,
        temperatureMin,
        temperatureMax,
        StatutVehicule.DISPONIBLE);
  }

  public static Remorque reconstituer(
      UUID id,
      Immatriculation immatriculation,
      TypeCarrosserie carrosserie,
      Capacite capaciteUtile,
      boolean groupeFroid,
      Double temperatureMin,
      Double temperatureMax,
      StatutVehicule statut) {
    return new Remorque(
        id,
        immatriculation,
        carrosserie,
        capaciteUtile,
        groupeFroid,
        temperatureMin,
        temperatureMax,
        statut);
  }

  public void changerStatut(StatutVehicule nouveauStatut) {
    this.statut = Objects.requireNonNull(nouveauStatut, "Le nouveau statut est obligatoire");
  }

  public void mettreAJourCapacite(Capacite capaciteUtile) {
    this.capaciteUtile = Objects.requireNonNull(capaciteUtile, "La capacité utile est obligatoire");
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

  public TypeCarrosserie carrosserie() {
    return carrosserie;
  }

  public Capacite capaciteUtile() {
    return capaciteUtile;
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

  public StatutVehicule statut() {
    return statut;
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
