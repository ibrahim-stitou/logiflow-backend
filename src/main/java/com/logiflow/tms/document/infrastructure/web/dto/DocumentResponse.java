package com.logiflow.tms.document.infrastructure.web.dto;

import com.logiflow.tms.document.domain.model.Document;
import java.time.LocalDate;
import java.util.UUID;

public record DocumentResponse(
    UUID id,
    String typeEntite,
    UUID entiteId,
    String typeDocument,
    String reference,
    String url,
    LocalDate dateExpiration) {

  public static DocumentResponse depuis(Document document) {
    return new DocumentResponse(
        document.id(),
        document.typeEntite().name(),
        document.entiteId(),
        document.typeDocument().name(),
        document.reference(),
        document.url(),
        document.dateExpiration());
  }
}
