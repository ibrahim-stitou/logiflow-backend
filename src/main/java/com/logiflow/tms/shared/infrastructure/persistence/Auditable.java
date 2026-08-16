package com.logiflow.tms.shared.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

/** Contrat technique commun aux entités auditées, exposé indépendamment de l'implémentation JPA. */
public interface Auditable {

  UUID getId();

  Instant getCreatedAt();

  String getCreatedBy();

  Instant getUpdatedAt();

  String getUpdatedBy();
}
