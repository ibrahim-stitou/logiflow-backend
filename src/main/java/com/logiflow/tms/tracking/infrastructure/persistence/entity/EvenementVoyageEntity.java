package com.logiflow.tms.tracking.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA de l'événement de voyage, isolée du modèle de domaine {@link
 * com.logiflow.tms.tracking.domain.model.EvenementVoyage}.
 */
@Getter
@Entity
@Table(name = "evenement_voyage", schema = "tracking")
public class EvenementVoyageEntity extends BaseEntity {

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "voyage_id", nullable = false)
  private UUID voyageId;

  @Column(name = "type_evenement", nullable = false, length = 30)
  private String typeEvenement;

  @Column(name = "horodatage", nullable = false)
  private Instant horodatage;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @Column(name = "commentaire", length = 1000)
  private String commentaire;

  protected EvenementVoyageEntity() {}

  @Builder
  public EvenementVoyageEntity(
      UUID id,
      UUID tenantId,
      UUID voyageId,
      String typeEvenement,
      Instant horodatage,
      Double latitude,
      Double longitude,
      String commentaire) {
    definirId(id);
    this.tenantId = tenantId;
    this.voyageId = voyageId;
    this.typeEvenement = typeEvenement;
    this.horodatage = horodatage;
    this.latitude = latitude;
    this.longitude = longitude;
    this.commentaire = commentaire;
  }
}
