package com.logiflow.tms.shared.infrastructure.security;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

/** Point d'accès unique au contexte de sécurité courant, utilisé par l'audit et les contrôleurs. */
@Service
public class SecurityContextService {

  /**
   * Renvoie l'utilisateur authentifié courant, vide si non authentifié (contexte système/batch).
   */
  public Optional<CurrentUser> utilisateurCourant() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }
    if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
      Jwt jwt = jwtAuthentication.getToken();
      Set<String> roles =
          jwtAuthentication.getAuthorities().stream()
              .map(authority -> authority.getAuthority())
              .collect(Collectors.toUnmodifiableSet());
      String nomAffichage = jwt.getClaimAsString("preferred_username");
      return Optional.of(
          new CurrentUser(
              jwt.getSubject(), nomAffichage != null ? nomAffichage : jwt.getSubject(), roles));
    }
    // Authentification non-JWT : utilisateur fictif du profil local (LocalDevAuthenticationFilter)
    // ou jeton de contexte du copilote. Les anonymes restent exclus.
    if (authentication instanceof UsernamePasswordAuthenticationToken) {
      Set<String> roles =
          authentication.getAuthorities().stream()
              .map(authority -> authority.getAuthority())
              .collect(Collectors.toUnmodifiableSet());
      return Optional.of(
          new CurrentUser(authentication.getName(), authentication.getName(), roles));
    }
    return Optional.empty();
  }

  /** Identifiant à utiliser pour l'audit (createdBy/updatedBy), avec repli sur "system". */
  public String identifiantPourAudit() {
    return utilisateurCourant().map(CurrentUser::sujet).orElse(CurrentUser.SYSTEME);
  }
}
