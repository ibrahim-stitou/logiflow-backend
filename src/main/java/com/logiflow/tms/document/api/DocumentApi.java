package com.logiflow.tms.document.api;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrat public du module Document, seul point d'entrée autorisé pour les autres modules (ex.
 * {@code fleet} pour vérifier la conformité documentaire d'un véhicule ou d'une remorque).
 */
public interface DocumentApi {

  /**
   * Indique si tous les documents rattachés à l'entité désignée sont valides à la date donnée.
   * Une entité sans document rattaché est considérée conforme.
   *
   * @param typeEntite nom de la constante {@code TypeEntiteDocumentable} du module (ex. {@code
   *     "VEHICULE"})
   */
  boolean tousValides(String typeEntite, UUID entiteId, LocalDate date);
}
