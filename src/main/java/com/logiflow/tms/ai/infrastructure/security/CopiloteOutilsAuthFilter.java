package com.logiflow.tms.ai.infrastructure.security;

import com.logiflow.tms.ai.domain.port.out.ContexteCopiloteStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authentifie les rappels du service IA sur {@code /internal/copilote/**} :
 *
 * <ol>
 *   <li>{@code X-Internal-Api-Key} doit valoir la clé de rappel (distincte de celle que Spring
 *       présente au service IA) — comparaison en temps constant ;
 *   <li>{@code X-Copilote-Contexte} doit désigner un jeton émis par Spring pour un message en cours
 *       et non expiré.
 * </ol>
 *
 * L'utilisateur authentifié est alors celui du jeton, avec ses rôles réels : les outils appliquent
 * ses droits, jamais ceux que le service IA pourrait prétendre. Instancié par {@code
 * CopiloteSecurityConfig} (pas un {@code @Component}, sinon enregistré sur toutes les routes).
 */
public class CopiloteOutilsAuthFilter extends OncePerRequestFilter {

  public static final String HEADER_CLE = "X-Internal-Api-Key";
  public static final String HEADER_CONTEXTE = "X-Copilote-Contexte";

  private final byte[] cleAttendue;
  private final ContexteCopiloteStore contexteStore;

  public CopiloteOutilsAuthFilter(String cleAttendue, ContexteCopiloteStore contexteStore) {
    this.cleAttendue = cleAttendue.getBytes(StandardCharsets.UTF_8);
    this.contexteStore = contexteStore;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String cle = request.getHeader(HEADER_CLE);
    if (cle == null || !MessageDigest.isEqual(cleAttendue, cle.getBytes(StandardCharsets.UTF_8))) {
      refuser(response, "Clé de rappel manquante ou invalide.");
      return;
    }
    var contexte = contexteStore.trouver(request.getHeader(HEADER_CONTEXTE));
    if (contexte.isEmpty()) {
      refuser(response, "Jeton de contexte du copilote manquant, inconnu ou expiré.");
      return;
    }
    var authentification =
        UsernamePasswordAuthenticationToken.authenticated(
            contexte.get().utilisateurId(),
            null,
            contexte.get().roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList());
    SecurityContextHolder.getContext().setAuthentication(authentification);
    try {
      filterChain.doFilter(request, response);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private static void refuser(HttpServletResponse response, String detail) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response
        .getWriter()
        .write("{\"title\":\"Non authentifié\",\"status\":401,\"detail\":\"" + detail + "\"}");
  }
}
