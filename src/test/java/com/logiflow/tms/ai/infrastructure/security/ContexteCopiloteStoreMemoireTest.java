package com.logiflow.tms.ai.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.logiflow.tms.ai.domain.model.copilote.ContexteCopilote;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ContexteCopiloteStoreMemoireTest {

  private static final Instant MAINTENANT = Instant.parse("2026-09-23T10:00:00Z");

  private static ContexteCopilote contexte(String jeton, Instant expireLe) {
    return new ContexteCopilote(jeton, "user-1", "Alice", Set.of("EXPLOITANT"), expireLe);
  }

  @Test
  void unJetonValideEstRetrouvePuisRevoque() {
    var store = new ContexteCopiloteStoreMemoire(Clock.fixed(MAINTENANT, ZoneOffset.UTC));
    store.enregistrer(contexte("j1", MAINTENANT.plus(Duration.ofMinutes(5))));

    assertThat(store.trouver("j1"))
        .get()
        .extracting(ContexteCopilote::utilisateurId)
        .isEqualTo("user-1");
    store.revoquer("j1");
    assertThat(store.trouver("j1")).isEmpty();
  }

  @Test
  void unJetonExpireOuInconnuEstIgnore() {
    var store = new ContexteCopiloteStoreMemoire(Clock.fixed(MAINTENANT, ZoneOffset.UTC));
    store.enregistrer(contexte("expire", MAINTENANT));

    assertThat(store.trouver("expire")).isEmpty();
    assertThat(store.trouver("inconnu")).isEmpty();
    assertThat(store.trouver(null)).isEmpty();
  }
}
