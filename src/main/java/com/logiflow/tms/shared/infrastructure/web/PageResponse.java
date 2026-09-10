package com.logiflow.tms.shared.infrastructure.web;

import com.logiflow.tms.shared.application.Page;
import java.util.List;
import java.util.function.Function;

/** Enveloppe JSON standard pour toute réponse paginée exposée par l'API REST. */
public record PageResponse<T>(
    List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {

  public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
    List<T> contenu = page.contenu().stream().map(mapper).toList();
    return new PageResponse<>(
        contenu, page.numero(), page.taille(), page.totalElements(), page.totalPages());
  }

  /** Réponse non paginée pour les filtres (ex. par commande ou par dossier). */
  public static <T> PageResponse<T> ofList(List<T> content) {
    int size = content.size();
    return new PageResponse<>(content, 0, Math.max(size, 1), size, size == 0 ? 0 : 1);
  }
}
