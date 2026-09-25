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
@Table(name = "contrat_assurance", schema = "maintenance")
public class ContratAssuranceEntity extends BaseEntity {

  @Column(name = "assureur_id", nullable = false)
  private UUID assureurId;

  @Column(name = "numero_police", nullable = false, length = 60)
  private String numeroPolice;

  @Column(name = "type_contrat", nullable = false, length = 20)
  private String typeContrat;

  @Column(name = "garanties_json", nullable = false, columnDefinition = "text")
  private String garantiesJson;

  @Column(name = "franchise", precision = 12, scale = 2)
  private BigDecimal franchise;

  @Column(name = "prime_annuelle", precision = 12, scale = 2)
  private BigDecimal primeAnnuelle;

  @Column(name = "date_effet", nullable = false)
  private LocalDate dateEffet;

  @Column(name = "date_echeance", nullable = false)
  private LocalDate dateEcheance;

  @Column(name = "engins_json", nullable = false, columnDefinition = "text")
  private String enginsJson;

  @Column(name = "actif", nullable = false)
  private boolean actif;

  public ContratAssuranceEntity() {}

  public ContratAssuranceEntity(UUID id) {
    definirId(id);
  }
}
