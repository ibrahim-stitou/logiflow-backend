package com.logiflow.tms.document.infrastructure.persistence.mapper;

import com.logiflow.tms.document.domain.model.Document;
import com.logiflow.tms.document.domain.model.TypeDocument;
import com.logiflow.tms.document.domain.model.TypeEntiteDocumentable;
import com.logiflow.tms.document.infrastructure.persistence.entity.DocumentEntity;
import org.mapstruct.Mapper;

/** Traduit entre le modèle de domaine {@link Document} et l'entité JPA {@link DocumentEntity}. */
@Mapper(componentModel = "spring")
public interface DocumentMapper {

  default Document versDomaine(DocumentEntity entity) {
    if (entity == null) {
      return null;
    }
    return Document.reconstituer(
        entity.getId(),
        TypeEntiteDocumentable.valueOf(entity.getTypeEntite()),
        entity.getEntiteId(),
        TypeDocument.valueOf(entity.getTypeDocument()),
        entity.getReference(),
        entity.getUrl(),
        entity.getDateExpiration());
  }

  default DocumentEntity versEntite(Document document) {
    if (document == null) {
      return null;
    }
    return DocumentEntity.builder()
        .id(document.id())
        .typeEntite(document.typeEntite().name())
        .entiteId(document.entiteId())
        .typeDocument(document.typeDocument().name())
        .reference(document.reference())
        .url(document.url())
        .dateExpiration(document.dateExpiration())
        .build();
  }
}
