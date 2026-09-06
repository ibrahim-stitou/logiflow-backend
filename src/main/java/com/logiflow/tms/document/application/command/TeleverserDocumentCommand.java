package com.logiflow.tms.document.application.command;

import com.logiflow.tms.document.domain.model.TypeDocument;
import com.logiflow.tms.document.domain.model.TypeEntiteDocumentable;
import java.time.LocalDate;
import java.util.UUID;

/** Commande applicative de téléversement d'un document. */
public record TeleverserDocumentCommand(
    TypeEntiteDocumentable typeEntite,
    UUID entiteId,
    TypeDocument typeDocument,
    String reference,
    LocalDate dateExpiration,
    String nomFichier,
    String typeContenu,
    byte[] contenu) {}
