package com.logiflow.tms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authentifie un utilisateur fictif de développement. Activé uniquement via {@code
 * logiflow.security.anonymous-local-access=true} (profil {@code local}). Permet au frontend Angular
 * d'appeler l'API sans Keycloak. Inactif en test, dev et prod.
 */
public class LocalDevAuthenticationFilter extends OncePerRequestFilter {

  static final String LOCAL_DEV_PRINCIPAL = "local-dev";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      var authentication =
          UsernamePasswordAuthenticationToken.authenticated(
              LOCAL_DEV_PRINCIPAL,
              "N/A",
              List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATEUR")));
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }

  /**
   * Le filtre doit aussi courir sur le dispatch ERROR : en session STATELESS le contexte d'auth de
   * la requête d'origine n'est pas reporté, et {@code OncePerRequestFilter} ignore ERROR par
   * défaut.
   */
  @Override
  protected boolean shouldNotFilterErrorDispatch() {
    return false;
  }
}
