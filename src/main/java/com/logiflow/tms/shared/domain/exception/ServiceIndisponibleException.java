package com.logiflow.tms.shared.domain.exception;

/**
 * Un système externe sollicité (service IA, moteur d'itinéraires...) est momentanément
 * indisponible. Traduite en HTTP 503 par le gestionnaire d'erreurs global.
 */
public class ServiceIndisponibleException extends RuntimeException {

  public ServiceIndisponibleException(String message) {
    super(message);
  }

  public ServiceIndisponibleException(String message, Throwable cause) {
    super(message, cause);
  }
}
