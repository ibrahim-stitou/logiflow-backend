package com.logiflow.tms.maintenance.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "plan_entretien", schema = "maintenance")
public class PlanEntretienEntity extends BaseEntity {

  @Column(name = "type_engin", nullable = false, length = 20)
  private String typeEngin;

  @Column(name = "engin_id", nullable = false)
  private UUID enginId;

  @Column(name = "libelle", nullable = false)
  private String libelle;

  @Column(name = "type_intervention", nullable = false, length = 30)
  private String typeIntervention;

  @Column(name = "periodicite_km")
  private Integer periodiciteKm;

  @Column(name = "periodicite_mois")
  private Integer periodiciteMois;

  @Column(name = "periodicite_heures")
  private Integer periodiciteHeures;

  @Column(name = "seuil_alerte_km", nullable = false)
  private int seuilAlerteKm;

  @Column(name = "seuil_alerte_jours", nullable = false)
  private int seuilAlerteJours;

  @Column(name = "duree_estimee_min", nullable = false)
  private int dureeEstimeeMin;

  @Column(name = "cout_estime", precision = 12, scale = 2)
  private BigDecimal coutEstime;

  @Column(name = "prestataire_id")
  private UUID prestataireId;

  @Column(name = "actif", nullable = false)
  private boolean actif;

  @Column(name = "derniere_date")
  private LocalDate derniereDate;

  @Column(name = "derniere_km")
  private Integer derniereKm;

  @Column(name = "derniere_heures")
  private Integer derniereHeures;

  public PlanEntretienEntity() {}

  public PlanEntretienEntity(UUID id) {
    definirId(id);
  }
}
