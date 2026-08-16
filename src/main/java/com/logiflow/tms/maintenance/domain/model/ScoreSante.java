package com.logiflow.tms.maintenance.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Score de santé d'un véhicule à une date donnée, avec statut dérivé et échéance projetée. */
public final class ScoreSante {

  private final UUID id;
  private final UUID vehiculeId;
  private final LocalDate calculeLe;
  private final double score;
  private final StatutSante statut;
  private final int kmAvantEcheance;
  private final LocalDate dateEcheanceProjetee;
  private final String recommandation;

  private ScoreSante(
      UUID id,
      UUID vehiculeId,
      LocalDate calculeLe,
      double score,
      StatutSante statut,
      int kmAvantEcheance,
      LocalDate dateEcheanceProjetee,
      String recommandation) {
    this.id = Objects.requireNonNull(id, "L'identifiant du score de santé est obligatoire");
    this.vehiculeId = Objects.requireNonNull(vehiculeId, "Le véhicule est obligatoire");
    this.calculeLe = Objects.requireNonNull(calculeLe, "La date de calcul est obligatoire");
    if (score < 0 || score > 100) {
      throw new IllegalArgumentException("Le score doit être compris entre 0 et 100");
    }
    this.score = score;
    this.statut = Objects.requireNonNull(statut, "Le statut de santé est obligatoire");
    this.kmAvantEcheance = kmAvantEcheance;
    this.dateEcheanceProjetee =
        Objects.requireNonNull(dateEcheanceProjetee, "La date d'échéance projetée est obligatoire");
    this.recommandation = recommandation;
  }

  /** Calcule un score de santé et en dérive automatiquement le statut. */
  public static ScoreSante calculer(
      UUID id,
      UUID vehiculeId,
      LocalDate calculeLe,
      double score,
      int kmAvantEcheance,
      LocalDate dateEcheanceProjetee,
      String recommandation) {
    return new ScoreSante(
        id,
        vehiculeId,
        calculeLe,
        score,
        deriverStatut(score, kmAvantEcheance),
        kmAvantEcheance,
        dateEcheanceProjetee,
        recommandation);
  }

  public static ScoreSante reconstituer(
      UUID id,
      UUID vehiculeId,
      LocalDate calculeLe,
      double score,
      StatutSante statut,
      int kmAvantEcheance,
      LocalDate dateEcheanceProjetee,
      String recommandation) {
    return new ScoreSante(
        id,
        vehiculeId,
        calculeLe,
        score,
        statut,
        kmAvantEcheance,
        dateEcheanceProjetee,
        recommandation);
  }

  private static StatutSante deriverStatut(double score, int kmAvantEcheance) {
    if (kmAvantEcheance <= 0) {
      return StatutSante.CRITIQUE;
    }
    if (score >= 80) {
      return StatutSante.BON;
    }
    if (score >= 60) {
      return StatutSante.SURVEILLER;
    }
    if (score >= 40) {
      return StatutSante.A_PLANIFIER;
    }
    return StatutSante.CRITIQUE;
  }

  public boolean necessiteIntervention() {
    return statut == StatutSante.A_PLANIFIER || statut == StatutSante.CRITIQUE;
  }

  public UUID id() {
    return id;
  }

  public UUID vehiculeId() {
    return vehiculeId;
  }

  public LocalDate calculeLe() {
    return calculeLe;
  }

  public double score() {
    return score;
  }

  public StatutSante statut() {
    return statut;
  }

  public int kmAvantEcheance() {
    return kmAvantEcheance;
  }

  public LocalDate dateEcheanceProjetee() {
    return dateEcheanceProjetee;
  }

  public String recommandation() {
    return recommandation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ScoreSante that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
