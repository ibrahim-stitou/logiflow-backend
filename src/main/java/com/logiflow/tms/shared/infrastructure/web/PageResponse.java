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
}
