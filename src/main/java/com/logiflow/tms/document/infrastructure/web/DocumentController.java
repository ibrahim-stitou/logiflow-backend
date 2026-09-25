package com.logiflow.tms.document.infrastructure.web;

import com.logiflow.tms.document.application.DocumentService;
import com.logiflow.tms.document.application.command.TeleverserDocumentCommand;
import com.logiflow.tms.document.domain.model.TypeDocument;
import com.logiflow.tms.document.domain.model.TypeEntiteDocumentable;
import com.logiflow.tms.document.infrastructure.web.dto.DocumentResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST du module Document : téléversement, consultation et suppression. */
@RestController
@RequiredArgsConstructor
public class DocumentController {

  private final DocumentService documentService;

  @PostMapping(value = "/api/v1/documents", consumes = "multipart/form-data")
  public ResponseEntity<DocumentResponse> televerser(
      @RequestPart("fichier") MultipartFile fichier,
      @RequestParam @NotNull TypeEntiteDocumentable typeEntite,
      @RequestParam @NotNull UUID entiteId,
      @RequestParam @NotNull TypeDocument typeDocument,
      @RequestParam(required = false) String reference,
      @RequestParam(required = false) LocalDate dateExpiration)
      throws IOException {
    UUID id =
        documentService.televerser(
            new TeleverserDocumentCommand(
                typeEntite,
                entiteId,
                typeDocument,
                reference,
                dateExpiration,
                fichier.getOriginalFilename(),
                fichier.getContentType(),
                fichier.getBytes()));
    DocumentResponse reponse =
        documentService.lister(typeEntite, entiteId).stream()
            .filter(document -> document.id().equals(id))
            .findFirst()
            .map(DocumentResponse::depuis)
            .orElseThrow();
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/documents")
  public List<DocumentResponse> lister(
      @RequestParam TypeEntiteDocumentable typeEntite, @RequestParam UUID entiteId) {
    return documentService.lister(typeEntite, entiteId).stream()
        .map(DocumentResponse::depuis)
        .toList();
  }

  @DeleteMapping("/api/v1/documents/{id}")
  public ResponseEntity<Void> supprimer(@PathVariable UUID id) {
    documentService.supprimer(id);
    return ResponseEntity.noContent().build();
  }
}
