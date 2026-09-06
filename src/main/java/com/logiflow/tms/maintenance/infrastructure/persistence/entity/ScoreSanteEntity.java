package com.logiflow.tms.maintenance.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "score_sante", schema = "maintenance")
public class ScoreSanteEntity extends BaseEntity {

  @Column(name = "vehicule_id", nullable = false)
  private UUID vehiculeId;

  @Column(name = "calcule_le", nullable = false)
  private LocalDate calculeLe;

  @Column(name = "score", nullable = false)
  private double score;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "km_avant_echeance", nullable = false)
  private int kmAvantEcheance;

  @Column(name = "date_echeance_projetee", nullable = false)
  private LocalDate dateEcheanceProjetee;

  @Column(name = "recommandation", length = 1000)
  private String recommandation;

  protected ScoreSanteEntity() {}

  @Builder
  public ScoreSanteEntity(
      UUID id,
      UUID vehiculeId,
      LocalDate calculeLe,
      double score,
      String statut,
      int kmAvantEcheance,
      LocalDate dateEcheanceProjetee,
      String recommandation) {
    definirId(id);
    this.vehiculeId = vehiculeId;
    this.calculeLe = calculeLe;
    this.score = score;
    this.statut = statut;
    this.kmAvantEcheance = kmAvantEcheance;
    this.dateEcheanceProjetee = dateEcheanceProjetee;
    this.recommandation = recommandation;
  }
}
