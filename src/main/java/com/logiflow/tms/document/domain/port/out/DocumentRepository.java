package com.logiflow.tms.document.domain.port.out;

import com.logiflow.tms.document.domain.model.Document;
import com.logiflow.tms.document.domain.model.TypeEntiteDocumentable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie pour la persistance des documents. */
public interface DocumentRepository {

  Document sauvegarder(Document document);

  Optional<Document> parId(UUID id);

  List<Document> parEntite(TypeEntiteDocumentable typeEntite, UUID entiteId);

  void supprimer(UUID id);
}
