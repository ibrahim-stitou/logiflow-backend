package com.logiflow.tms.shared.application;

/**
 * Résultat d'un cas d'utilisation applicatif, alternative aux exceptions pour les échecs attendus
 * (ex. refus métier remonté à l'appelant sans rompre le flux).
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

  static <T> Result<T> success(T valeur) {
    return new Success<>(valeur);
  }

  static <T> Result<T> failure(String message) {
    return new Failure<>(message);
  }

  boolean estUnSucces();

  record Success<T>(T valeur) implements Result<T> {
    @Override
    public boolean estUnSucces() {
      return true;
    }
  }

  record Failure<T>(String message) implements Result<T> {
    @Override
    public boolean estUnSucces() {
      return false;
    }
  }
}
