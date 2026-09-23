package com.logiflow.tms.document.application;

import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.document.api.DocumentEntiteModificationEvent;
import com.logiflow.tms.document.application.command.TeleverserDocumentCommand;
import com.logiflow.tms.document.domain.model.Document;
import com.logiflow.tms.document.domain.model.TypeEntiteDocumentable;
import com.logiflow.tms.document.domain.port.out.DocumentRepository;
import com.logiflow.tms.document.domain.port.out.FileStorageService;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du module Document. */
@Service
@RequiredArgsConstructor
public class DocumentService implements DocumentApi {

  private final DocumentRepository documentRepository;
  private final FileStorageService fileStorageService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public UUID televerser(TeleverserDocumentCommand command) {
    eventPublisher.publishEvent(
        new DocumentEntiteModificationEvent(command.typeEntite().name(), command.entiteId()));
    String url =
        fileStorageService.stocker(command.nomFichier(), command.contenu(), command.typeContenu());
    Document document =
        Document.creer(
            UUID.randomUUID(),
            command.typeEntite(),
            command.entiteId(),
            command.typeDocument(),
            command.reference(),
            url,
            command.dateExpiration());
    return documentRepository.sauvegarder(document).id();
  }

  @Transactional(readOnly = true)
  public List<Document> lister(TypeEntiteDocumentable typeEntite, UUID entiteId) {
    return documentRepository.parEntite(typeEntite, entiteId);
  }

  @Transactional
  public void supprimer(UUID id) {
    Document document =
        documentRepository
            .parId(id)
            .orElseThrow(
                () -> new NotFoundException("Aucun document trouvé pour l'identifiant " + id));
    eventPublisher.publishEvent(
        new DocumentEntiteModificationEvent(document.typeEntite().name(), document.entiteId()));
    fileStorageService.supprimer(document.url());
    documentRepository.supprimer(id);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean tousValides(String typeEntite, UUID entiteId, LocalDate date) {
    List<Document> documents = lister(TypeEntiteDocumentable.valueOf(typeEntite), entiteId);
    return Document.tousValides(documents, date);
  }
}
