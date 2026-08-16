package com.logiflow.tms.maintenance.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Plan d'entretien périodique suivi pour un véhicule. */
public final class PlanEntretien {

  private final UUID id;
  private final UUID vehiculeId;
  private String libelle;
  private Integer periodiciteKm;
  private Integer periodiciteMois;
  private int seuilAlerteKm;
  private int dureeEstimeeMin;

  private PlanEntretien(
      UUID id,
      UUID vehiculeId,
      String libelle,
      Integer periodiciteKm,
      Integer periodiciteMois,
      int seuilAlerteKm,
      int dureeEstimeeMin) {
    this.id = Objects.requireNonNull(id, "L'identifiant du plan d'entretien est obligatoire");
    this.vehiculeId = Objects.requireNonNull(vehiculeId, "Le véhicule est obligatoire");
    this.libelle = validerLibelle(libelle);
    if (periodiciteKm == null && periodiciteMois == null) {
      throw new IllegalArgumentException(
          "Un plan d'entretien doit définir une périodicité en kilomètres ou en mois");
    }
    if (periodiciteKm != null && periodiciteKm <= 0) {
      throw new IllegalArgumentException(
          "La périodicité en kilomètres doit être strictement positive");
    }
    if (periodiciteMois != null && periodiciteMois <= 0) {
      throw new IllegalArgumentException("La périodicité en mois doit être strictement positive");
    }
    this.periodiciteKm = periodiciteKm;
    this.periodiciteMois = periodiciteMois;
    if (seuilAlerteKm < 0) {
      throw new IllegalArgumentException("Le seuil d'alerte ne peut pas être négatif");
    }
    if (dureeEstimeeMin < 0) {
      throw new IllegalArgumentException("La durée estimée ne peut pas être négative");
    }
    this.seuilAlerteKm = seuilAlerteKm;
    this.dureeEstimeeMin = dureeEstimeeMin;
  }

  public static PlanEntretien creer(
      UUID id,
      UUID vehiculeId,
      String libelle,
      Integer periodiciteKm,
      Integer periodiciteMois,
      int seuilAlerteKm,
      int dureeEstimeeMin) {
    return new PlanEntretien(
        id, vehiculeId, libelle, periodiciteKm, periodiciteMois, seuilAlerteKm, dureeEstimeeMin);
  }

  public static PlanEntretien reconstituer(
      UUID id,
      UUID vehiculeId,
      String libelle,
      Integer periodiciteKm,
      Integer periodiciteMois,
      int seuilAlerteKm,
      int dureeEstimeeMin) {
    return new PlanEntretien(
        id, vehiculeId, libelle, periodiciteKm, periodiciteMois, seuilAlerteKm, dureeEstimeeMin);
  }

  private static String validerLibelle(String libelle) {
    Objects.requireNonNull(libelle, "Le libellé est obligatoire");
    if (libelle.isBlank()) {
      throw new IllegalArgumentException("Le libellé ne peut pas être vide");
    }
    return libelle.strip();
  }

  /** Indique si le kilométrage restant avant échéance franchit le seuil d'alerte configuré. */
  public boolean alerteFranchie(int kmRestantAvantEcheance) {
    return kmRestantAvantEcheance <= seuilAlerteKm;
  }

  public UUID id() {
    return id;
  }

  public UUID vehiculeId() {
    return vehiculeId;
  }

  public String libelle() {
    return libelle;
  }

  public Integer periodiciteKm() {
    return periodiciteKm;
  }

  public Integer periodiciteMois() {
    return periodiciteMois;
  }

  public int seuilAlerteKm() {
    return seuilAlerteKm;
  }

  public int dureeEstimeeMin() {
    return dureeEstimeeMin;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PlanEntretien that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
