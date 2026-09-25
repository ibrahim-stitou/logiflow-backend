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

@Getter
@Setter
@Entity
@Table(name = "sinistre", schema = "maintenance")
public class SinistreEntity extends BaseEntity {

  @Column(name = "reference", nullable = false, length = 20)
  private String reference;

  @Column(name = "vehicule_id")
  private UUID vehiculeId;

  @Column(name = "remorque_id")
  private UUID remorqueId;

  @Column(name = "chauffeur_id")
  private UUID chauffeurId;

  @Column(name = "voyage_id")
  private UUID voyageId;

  @Column(name = "date_survenance", nullable = false)
  private LocalDateTime dateSurvenance;

  @Column(name = "lieu", length = 500)
  private String lieu;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @Column(name = "type_sinistre", nullable = false, length = 30)
  private String typeSinistre;

  @Column(name = "gravite", nullable = false, length = 20)
  private String gravite;

  @Column(name = "responsabilite", nullable = false, length = 20)
  private String responsabilite;

  @Column(name = "description", nullable = false, columnDefinition = "text")
  private String description;

  @Column(name = "constat_amiable", nullable = false)
  private boolean constatAmiable;

  @Column(name = "rapport_police", nullable = false)
  private boolean rapportPolice;

  @Column(name = "blesses", nullable = false)
  private boolean blesses;

  @Column(name = "engin_immobilise", nullable = false)
  private boolean enginImmobilise;

  @Column(name = "tiers_nom")
  private String tiersNom;

  @Column(name = "tiers_immatriculation", length = 20)
  private String tiersImmatriculation;

  @Column(name = "tiers_assureur")
  private String tiersAssureur;

  @Column(name = "tiers_numero_police", length = 60)
  private String tiersNumeroPolice;

  @Column(name = "contrat_id")
  private UUID contratId;

  @Column(name = "numero_dossier_assureur", length = 60)
  private String numeroDossierAssureur;

  @Column(name = "date_declaration_assureur")
  private LocalDate dateDeclarationAssureur;

  @Column(name = "expert_id")
  private UUID expertId;

  @Column(name = "date_expertise")
  private LocalDate dateExpertise;

  @Column(name = "estimation_dommages", precision = 12, scale = 2)
  private BigDecimal estimationDommages;

  @Column(name = "franchise", precision = 12, scale = 2)
  private BigDecimal franchise;

  @Column(name = "indemnite", precision = 12, scale = 2)
  private BigDecimal indemnite;

  @Column(name = "statut", nullable = false, length = 20)
  private String statut;

  @Column(name = "date_cloture")
  private LocalDate dateCloture;

  public SinistreEntity() {}

  public SinistreEntity(UUID id) {
    definirId(id);
  }
}
