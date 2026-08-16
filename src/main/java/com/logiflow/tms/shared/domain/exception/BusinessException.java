package com.logiflow.tms.shared.domain.exception;

/** Violation d'une règle métier. Traduite en HTTP 422 par le gestionnaire d'erreurs global. */
public class BusinessException extends RuntimeException {

  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
  }
}
