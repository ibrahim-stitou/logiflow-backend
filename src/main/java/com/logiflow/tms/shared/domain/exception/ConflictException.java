package com.logiflow.tms.shared.domain.exception;

/** Conflit d'état (doublon, verrouillage optimiste...). Traduite en HTTP 409. */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
