package com.logiflow.tms.maintenance.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** Ordre de travail ; les lignes de coût sont sérialisées en JSON, les totaux dénormalisés. */
@Getter
@Setter
@Entity
@Table(name = "ordre_travail", schema = "maintenance")
public class OrdreTravailEntity extends BaseEntity {

  @Column(name = "reference", nullable = false, length = 20)
  private String reference;

  @Column(name = "type_engin", nullable = false, length = 20)
  private String typeEngin;

  @Column(name = "engin_id", nullable = false)
  private UUID enginId;

  @Column(name = "origine", nullable = false, length = 20)
  private String origine;

  @Column(name = "plan_id")
  private UUID planId;

  @Column(name = "sinistre_id")
  private UUID sinistreId;

  @Column(name = "type_intervention", nullable = false, length = 30)
  private String typeIntervention;

  @Column(name = "nature", nullable = false, length = 20)
  private String nature;

  @Column(name = "priorite", nullable = false, length = 10)
  private String priorite;

  @Column(name = "titre", nullable = false)
  private String titre;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "prestataire_id")
  private UUID prestataireId;

  @Column(name = "debut_planifie", nullable = false)
  private LocalDateTime debutPlanifie;

  @Column(name = "fin_planifiee")
  private LocalDateTime finPlanifiee;

  @Column(name = "immobilisation", nullable = false)
  private boolean immobilisation;

  @Column(name = "budget_estime", precision = 12, scale = 2)
  private BigDecimal budgetEstime;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "lignes_json", nullable = false, columnDefinition = "text")
  private String lignesJson;

  @Column(name = "total_ht", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalHt;

  @Column(name = "total_ttc", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalTtc;

  @Column(name = "debut_reel")
  private LocalDateTime debutReel;

  @Column(name = "fin_reelle")
  private LocalDateTime finReelle;

  @Column(name = "kilometrage")
  private Integer kilometrage;

  @Column(name = "heures")
  private Integer heures;

  @Column(name = "diagnostic", columnDefinition = "text")
  private String diagnostic;

  @Column(name = "travaux_realises", columnDefinition = "text")
  private String travauxRealises;

  @Column(name = "intervenant", length = 150)
  private String intervenant;

  @Column(name = "numero_facture", length = 60)
  private String numeroFacture;

  @Column(name = "date_facture")
  private LocalDate dateFacture;

  public OrdreTravailEntity() {}

  public OrdreTravailEntity(UUID id) {
    definirId(id);
  }
}
