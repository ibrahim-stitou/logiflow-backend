package com.logiflow.tms.shared.domain.exception;

import java.util.List;

/**
 * Échec de validation métier portant une ou plusieurs violations détaillées. Traduite en HTTP 400
 * par le gestionnaire d'erreurs global.
 */
public class ValidationException extends RuntimeException {

  private final List<String> violations;

  public ValidationException(String message, List<String> violations) {
    super(message);
    this.violations = List.copyOf(violations);
  }

  public List<String> violations() {
    return violations;
  }
}
