package com.logiflow.tms.document.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.logiflow.tms.document.domain.model.Document;
import com.logiflow.tms.document.domain.model.TypeDocument;
import com.logiflow.tms.document.domain.model.TypeEntiteDocumentable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentTest {

  @Test
  void unDocumentExpireRendLEnsembleNonConforme() {
    Document assuranceExpiree =
        Document.creer(
            UUID.randomUUID(),
            TypeEntiteDocumentable.VEHICULE,
            UUID.randomUUID(),
            TypeDocument.ASSURANCE,
            "ASS-001",
            "/fichiers/assurance.pdf",
            LocalDate.of(2020, 1, 1));

    assertThat(Document.tousValides(List.of(assuranceExpiree), LocalDate.of(2026, 1, 1))).isFalse();
  }

  @Test
  void sansDocumentExpireLEnsembleEstConforme() {
    Document assuranceValide =
        Document.creer(
            UUID.randomUUID(),
            TypeEntiteDocumentable.VEHICULE,
            UUID.randomUUID(),
            TypeDocument.ASSURANCE,
            "ASS-002",
            "/fichiers/assurance.pdf",
            LocalDate.of(2030, 1, 1));

    assertThat(Document.tousValides(List.of(assuranceValide), LocalDate.of(2026, 1, 1))).isTrue();
  }

  @Test
  void unDocumentSansDateDExpirationEstToujoursValide() {
    Document photo =
        Document.creer(
            UUID.randomUUID(),
            TypeEntiteDocumentable.VEHICULE,
            UUID.randomUUID(),
            TypeDocument.PHOTO,
            null,
            "/fichiers/photo.jpg",
            null);

    assertThat(photo.estValide(LocalDate.of(2099, 1, 1))).isTrue();
  }

  @Test
  void uneEntiteSansDocumentEstConforme() {
    assertThat(Document.tousValides(List.of(), LocalDate.now())).isTrue();
  }
}
