package com.logiflow.tms.shared.application;

import java.util.List;
import java.util.Objects;

/**
 * Page de résultats indépendante de Spring Data, utilisée dans les ports applicatifs pour ne pas
 * faire fuiter de type d'infrastructure vers le domaine ou les contrats d'API.
 */
public record Page<T>(List<T> contenu, int numero, int taille, long totalElements) {

  public Page {
    Objects.requireNonNull(contenu, "Le contenu de la page est obligatoire");
    contenu = List.copyOf(contenu);
  }

  public static <T> Page<T> of(List<T> contenu, int numero, int taille, long totalElements) {
    return new Page<>(contenu, numero, taille, totalElements);
  }

  public int totalPages() {
    return taille == 0 ? 0 : (int) Math.ceil((double) totalElements / taille);
  }

  public boolean estVide() {
    return contenu.isEmpty();
  }
}
