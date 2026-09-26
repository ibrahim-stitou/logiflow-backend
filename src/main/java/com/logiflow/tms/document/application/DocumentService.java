package com.logiflow.tms.document.application;

import com.logiflow.tms.document.api.DocumentApi;
import com.logiflow.tms.document.api.DocumentEntiteModificationEvent;
import com.logiflow.tms.document.api.DocumentSummary;
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

  /**
   * Contenu binaire d'un document pour téléchargement authentifié (évite d'exposer {@code
   * /fichiers/**} au navigateur sans proxy / jeton).
   */
  @Transactional(readOnly = true)
  public ContenuDocument lireContenu(UUID id) {
    Document document =
        documentRepository
            .parId(id)
            .orElseThrow(
                () -> new NotFoundException("Aucun document trouvé pour l'identifiant " + id));
    byte[] octets = fileStorageService.lire(document.url());
    String nomFichier = nomDepuisUrl(document.url());
    return new ContenuDocument(octets, nomFichier, typeMimeDepuisNom(nomFichier));
  }

  public record ContenuDocument(byte[] octets, String nomFichier, String typeMime) {}

  private static String nomDepuisUrl(String url) {
    int slash = url.lastIndexOf('/');
    String brut = slash >= 0 ? url.substring(slash + 1) : url;
    // Stockage : « {uuid}-{nomOriginal} » (uuid = 36 caractères).
    if (brut.length() > 37 && brut.charAt(36) == '-') {
      return brut.substring(37);
    }
    return brut.isBlank() ? "document" : brut;
  }

  private static String typeMimeDepuisNom(String nomFichier) {
    String lower = nomFichier.toLowerCase();
    if (lower.endsWith(".pdf")) {
      return "application/pdf";
    }
    if (lower.endsWith(".png")) {
      return "image/png";
    }
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    if (lower.endsWith(".webp")) {
      return "image/webp";
    }
    return "application/octet-stream";
  }

  @Override
  @Transactional(readOnly = true)
  public List<DocumentSummary> lister(String typeEntite, UUID entiteId) {
    return lister(TypeEntiteDocumentable.valueOf(typeEntite), entiteId).stream()
        .map(d -> new DocumentSummary(d.typeDocument().name(), d.reference(), d.dateExpiration()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean tousValides(String typeEntite, UUID entiteId, LocalDate date) {
    List<Document> documents = lister(TypeEntiteDocumentable.valueOf(typeEntite), entiteId);
    return Document.tousValides(documents, date);
  }
}
