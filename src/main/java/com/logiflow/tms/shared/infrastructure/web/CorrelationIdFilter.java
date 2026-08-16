package com.logiflow.tms.shared.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lit ou génère un identifiant de corrélation par requête, le place dans le MDC (pour les logs
 * structurés) et le renvoie systématiquement en en-tête de réponse.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String HEADER_NAME = "X-Correlation-Id";
  public static final String MDC_KEY = "correlationId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String correlationId = request.getHeader(HEADER_NAME);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }
    try {
      MDC.put(MDC_KEY, correlationId);
      response.setHeader(HEADER_NAME, correlationId);
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  /** Identifiant de corrélation de la requête courante, ou {@code null} hors contexte HTTP. */
  public static String correlationIdCourant() {
    return MDC.get(MDC_KEY);
  }
}
