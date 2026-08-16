package com.logiflow.tms.referential.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

/**
 * Entité JPA du site, isolée du modèle de domaine {@link
 * com.logiflow.tms.referential.domain.model.Site}.
 */
@Getter
@Entity
@Table(
    name = "site",
    schema = "referential",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_site_tenant_code",
            columnNames = {"tenant_id", "code"}))
public class SiteEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "libelle", nullable = false, length = 255)
  private String libelle;

  @Column(name = "client_id")
  private UUID clientId;

  @Column(name = "localisation", columnDefinition = "geography(Point,4326)", nullable = false)
  private Point localisation;

  @Column(name = "adresse", length = 500)
  private String adresse;

  @Column(name = "horaires_json", columnDefinition = "text")
  private String horairesJson;

  @Column(name = "contraintes_acces_json", columnDefinition = "text")
  private String contraintesAccesJson;

  @Column(name = "actif", nullable = false)
  private boolean actif;

  protected SiteEntity() {}

  @Builder
  public SiteEntity(
      UUID id,
      UUID tenantId,
      String code,
      String libelle,
      UUID clientId,
      Point localisation,
      String adresse,
      String horairesJson,
      String contraintesAccesJson,
      boolean actif) {
    definirId(id);
    this.tenantId = tenantId;
    this.code = code;
    this.libelle = libelle;
    this.clientId = clientId;
    this.localisation = localisation;
    this.adresse = adresse;
    this.horairesJson = horairesJson;
    this.contraintesAccesJson = contraintesAccesJson;
    this.actif = actif;
  }
}
