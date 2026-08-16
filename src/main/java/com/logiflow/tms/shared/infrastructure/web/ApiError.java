package com.logiflow.tms.shared.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Fabrique de réponses d'erreur conformes RFC 7807 ({@link ProblemDetail}), enrichies d'un
 * horodatage et de l'identifiant de corrélation de la requête.
 */
public final class ApiError {

  private static final String TYPE_BASE = "https://logiflow.tms/erreurs/";

  private ApiError() {}

  public static ProblemDetail of(
      HttpStatus status, String typeSlug, String title, String detail, HttpServletRequest request) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setType(URI.create(TYPE_BASE + typeSlug));
    problemDetail.setTitle(title);
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("correlationId", CorrelationIdFilter.correlationIdCourant());
    return problemDetail;
  }
}
