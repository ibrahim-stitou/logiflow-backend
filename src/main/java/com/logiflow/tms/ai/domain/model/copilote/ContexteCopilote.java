package com.logiflow.tms.ai.domain.model.copilote;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * Contexte d'autorisation d'UN message du copilote. Spring émet un jeton opaque aléatoire, le
 * transmet au service IA, qui le présente à chaque appel d'outil ({@code /internal/copilote/**}) :
 * l'utilisateur et ses rôles sont ainsi toujours résolus côté Spring, jamais crus sur parole du
 * service IA. Rôles sans préfixe {@code ROLE_}.
 */
public record ContexteCopilote(
    String jeton, String utilisateurId, String nomAffichage, Set<String> roles, Instant expireLe) {

  public ContexteCopilote {
    Objects.requireNonNull(jeton, "Le jeton est obligatoire");
    Objects.requireNonNull(utilisateurId, "L'utilisateur est obligatoire");
    Objects.requireNonNull(expireLe, "L'expiration est obligatoire");
    roles = roles != null ? Set.copyOf(roles) : Set.of();
  }

  public boolean estExpire(Instant maintenant) {
    return !maintenant.isBefore(expireLe);
  }
}
