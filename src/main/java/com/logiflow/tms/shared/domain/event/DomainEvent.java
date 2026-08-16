package com.logiflow.tms.shared.domain.event;

import java.time.Instant;

/** Contrat commun à tous les événements de domaine publiés entre modules. */
public interface DomainEvent {

  /** Horodatage de survenance de l'événement, pour traçabilité et rejeu éventuel. */
  Instant survenuLe();
}
