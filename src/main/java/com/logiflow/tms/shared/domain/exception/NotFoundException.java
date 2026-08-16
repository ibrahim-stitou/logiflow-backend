package com.logiflow.tms.shared.domain.exception;

/** Ressource introuvable. Traduite en HTTP 404 par le gestionnaire d'erreurs global. */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}
