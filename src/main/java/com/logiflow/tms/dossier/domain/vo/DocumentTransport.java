package com.logiflow.tms.dossier.domain.vo;

import java.util.Objects;

/** Document administratif ou commercial rattaché à un dossier de transport. */
public record DocumentTransport(
    TypeDocumentTransport type,
    String cheminFichier,
    boolean obligatoire,
    StatutDocumentTransport statut) {

  public DocumentTransport {
    Objects.requireNonNull(type, "Le type de document est obligatoire");
    Objects.requireNonNull(statut, "Le statut du document est obligatoire");
  }

  public enum TypeDocumentTransport {
    CMR,
    FACTURE,
    BON_LIVRAISON,
    DOUANE,
    AUTRE
  }

  public enum StatutDocumentTransport {
    MANQUANT,
    FOURNI,
    VALIDE
  }
}
