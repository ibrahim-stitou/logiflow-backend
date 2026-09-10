package com.logiflow.tms.document.infrastructure.persistence.entity;

import com.logiflow.tms.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Entité JPA du document, isolée du modèle de domaine {@link
 * com.logiflow.tms.document.domain.model.Document}. Table unique partagée par toutes les entités
 * documentables (relation polymorphe via {@code type_entite}/{@code entite_id}, sans clé étrangère
 * puisqu'elle peut pointer vers plusieurs tables propriétaires).
 */
@Getter
@Entity
@Table(name = "document", schema = "document")
public class DocumentEntity extends BaseEntity {

  @Column(name = "type_entite", nullable = false, length = 30)
  private String typeEntite;

  @Column(name = "entite_id", nullable = false)
  private UUID entiteId;

  @Column(name = "type_document", nullable = false, length = 30)
  private String typeDocument;

  @Column(name = "reference", length = 255)
  private String reference;

  @Column(name = "url", nullable = false, length = 500)
  private String url;

  @Column(name = "date_expiration")
  private LocalDate dateExpiration;

  protected DocumentEntity() {}

  @Builder
  public DocumentEntity(
      UUID id,
      String typeEntite,
      UUID entiteId,
      String typeDocument,
      String reference,
      String url,
      LocalDate dateExpiration) {
    definirId(id);
    this.typeEntite = typeEntite;
    this.entiteId = entiteId;
    this.typeDocument = typeDocument;
    this.reference = reference;
    this.url = url;
    this.dateExpiration = dateExpiration;
  }
}
