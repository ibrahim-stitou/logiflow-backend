package com.logiflow.tms.document.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Document (pièce administrative ou photo) rattaché à une entité de la flotte via une relation
 * polymorphe ({@link #typeEntite()}, {@link #entiteId()}), plutôt qu'à une clé étrangère dédiée par
 * table propriétaire.
 */
public final class Document {

  private final UUID id;
  private final TypeEntiteDocumentable typeEntite;
  private final UUID entiteId;
  private final TypeDocument typeDocument;
  private final String reference;
  private final String url;
  private final LocalDate dateExpiration;

  private Document(
      UUID id,
      TypeEntiteDocumentable typeEntite,
      UUID entiteId,
      TypeDocument typeDocument,
      String reference,
      String url,
      LocalDate dateExpiration) {
    this.id = Objects.requireNonNull(id, "L'identifiant du document est obligatoire");
    this.typeEntite =
        Objects.requireNonNull(typeEntite, "Le type d'entité rattachée est obligatoire");
    this.entiteId =
        Objects.requireNonNull(entiteId, "L'identifiant de l'entité rattachée est obligatoire");
    this.typeDocument = Objects.requireNonNull(typeDocument, "Le type de document est obligatoire");
    this.reference = reference;
    this.url = Objects.requireNonNull(url, "L'URL du document est obligatoire");
    this.dateExpiration = dateExpiration;
  }

  public static Document creer(
      UUID id,
      TypeEntiteDocumentable typeEntite,
      UUID entiteId,
      TypeDocument typeDocument,
      String reference,
      String url,
      LocalDate dateExpiration) {
    return new Document(id, typeEntite, entiteId, typeDocument, reference, url, dateExpiration);
  }

  public static Document reconstituer(
      UUID id,
      TypeEntiteDocumentable typeEntite,
      UUID entiteId,
      TypeDocument typeDocument,
      String reference,
      String url,
      LocalDate dateExpiration) {
    return new Document(id, typeEntite, entiteId, typeDocument, reference, url, dateExpiration);
  }

  /** Un document sans date d'expiration (ex. photo) est toujours valide. */
  public boolean estValide(LocalDate date) {
    Objects.requireNonNull(date, "La date de contrôle est obligatoire");
    return dateExpiration == null || !dateExpiration.isBefore(date);
  }

  /** Un ensemble de documents est conforme si chacun d'eux est valide à la date donnée. */
  public static boolean tousValides(List<Document> documents, LocalDate date) {
    return documents.stream().allMatch(document -> document.estValide(date));
  }

  public UUID id() {
    return id;
  }

  public TypeEntiteDocumentable typeEntite() {
    return typeEntite;
  }

  public UUID entiteId() {
    return entiteId;
  }

  public TypeDocument typeDocument() {
    return typeDocument;
  }

  public String reference() {
    return reference;
  }

  public String url() {
    return url;
  }

  public LocalDate dateExpiration() {
    return dateExpiration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Document document)) {
      return false;
    }
    return id.equals(document.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
