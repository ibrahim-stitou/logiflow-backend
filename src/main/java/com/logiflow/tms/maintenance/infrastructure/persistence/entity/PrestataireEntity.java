package com.logiflow.tms.maintenance.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "prestataire", schema = "maintenance")
public class PrestataireEntity extends BaseEntity {

  @Column(name = "code", nullable = false, length = 30)
  private String code;

  @Column(name = "raison_sociale", nullable = false)
  private String raisonSociale;

  @Column(name = "type_prestataire", nullable = false, length = 30)
  private String typePrestataire;

  @Column(name = "siret", length = 20)
  private String siret;

  @Column(name = "contact_nom", length = 150)
  private String contactNom;

  @Column(name = "telephone", length = 30)
  private String telephone;

  @Column(name = "email")
  private String email;

  @Column(name = "adresse", length = 500)
  private String adresse;

  @Column(name = "notes", columnDefinition = "text")
  private String notes;

  @Column(name = "actif", nullable = false)
  private boolean actif;

  public PrestataireEntity() {}

  public PrestataireEntity(UUID id) {
    definirId(id);
  }
}
