package com.logiflow.tms.ai.infrastructure.web;

import com.logiflow.tms.ai.application.outils.CatalogueOutilsCopilote;
import com.logiflow.tms.ai.application.outils.OutilCopilote;
import com.logiflow.tms.ai.application.outils.ResultatOutil;
import com.logiflow.tms.shared.infrastructure.security.SecurityContextService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Outils métier du copilote, appelés UNIQUEMENT par le service IA (tool-calling) — jamais par le
 * frontend. Authentification : clé de rappel + jeton de contexte ({@code CopiloteOutilsAuthFilter}
 * dans {@code CopiloteSecurityConfig}) ; l'utilisateur courant est celui qui a posé la question.
 */
@RestController
@RequestMapping("/internal/copilote/outils")
public class CopiloteOutilsController {

  private final CatalogueOutilsCopilote catalogue;
  private final SecurityContextService securityContextService;

  public CopiloteOutilsController(
      CatalogueOutilsCopilote catalogue, SecurityContextService securityContextService) {
    this.catalogue = catalogue;
    this.securityContextService = securityContextService;
  }

  public record OutilResponse(
      String nom, String libelle, String description, Map<String, Object> parametres) {

    static OutilResponse depuis(OutilCopilote outil) {
      return new OutilResponse(
          outil.nom(), outil.libelle(), outil.description(), outil.parametres());
    }
  }

  @GetMapping
  public List<OutilResponse> catalogue() {
    return catalogue.disponibles(roles()).stream().map(OutilResponse::depuis).toList();
  }

  @PostMapping("/{nom}")
  public ResultatOutil executer(
      @PathVariable String nom, @RequestBody(required = false) Map<String, Object> arguments) {
    return catalogue.executer(nom, arguments != null ? arguments : Map.of(), roles());
  }

  private Set<String> roles() {
    return securityContextService
        .utilisateurCourant()
        .orElseThrow(() -> new AccessDeniedException("Contexte du copilote requis"))
        .roles()
        .stream()
        .map(role -> role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role)
        .collect(Collectors.toUnmodifiableSet());
  }
}
