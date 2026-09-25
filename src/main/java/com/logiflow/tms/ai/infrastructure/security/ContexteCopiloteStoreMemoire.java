package com.logiflow.tms.ai.infrastructure.security;

import com.logiflow.tms.ai.domain.model.copilote.ContexteCopilote;
import com.logiflow.tms.ai.domain.port.out.ContexteCopiloteStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Jetons de contexte en mémoire : ils ne vivent que le temps d'un message (quelques secondes à
 * quelques minutes) et sont révoqués en fin de flux. En déploiement multi-instances, le service IA
 * rappelle l'instance qui a émis le jeton seulement si l'affinité est garantie ; sinon, remplacer
 * par un stockage partagé (Redis, table PostgreSQL).
 */
@Component
public class ContexteCopiloteStoreMemoire implements ContexteCopiloteStore {

  private final Map<String, ContexteCopilote> contextes = new ConcurrentHashMap<>();
  private final Clock horloge;

  public ContexteCopiloteStoreMemoire(Clock horloge) {
    this.horloge = horloge;
  }

  @Override
  public void enregistrer(ContexteCopilote contexte) {
    Instant maintenant = horloge.instant();
    // Purge opportuniste : les jetons non révoqués (crash en cours de flux) finissent par expirer.
    contextes.values().removeIf(c -> c.estExpire(maintenant));
    contextes.put(contexte.jeton(), contexte);
  }

  @Override
  public Optional<ContexteCopilote> trouver(String jeton) {
    if (jeton == null) {
      return Optional.empty();
    }
    ContexteCopilote contexte = contextes.get(jeton);
    if (contexte == null) {
      return Optional.empty();
    }
    if (contexte.estExpire(horloge.instant())) {
      contextes.remove(jeton);
      return Optional.empty();
    }
    return Optional.of(contexte);
  }

  @Override
  public void revoquer(String jeton) {
    contextes.remove(jeton);
  }
}
