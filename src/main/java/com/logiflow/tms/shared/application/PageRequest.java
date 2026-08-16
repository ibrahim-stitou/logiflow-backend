package com.logiflow.tms.shared.application;

/** Requête de pagination transverse, indépendante de Spring Data, utilisée par les ports out. */
public record PageRequest(int numero, int taille) {

  private static final int TAILLE_MAX = 100;

  public PageRequest {
    if (numero < 0) {
      throw new IllegalArgumentException("Le numéro de page ne peut pas être négatif");
    }
    if (taille < 1 || taille > TAILLE_MAX) {
      throw new IllegalArgumentException(
          "La taille de page doit être comprise entre 1 et " + TAILLE_MAX);
    }
  }

  public static PageRequest premiere(int taille) {
    return new PageRequest(0, taille);
  }

  public int offset() {
    return numero * taille;
  }
}
