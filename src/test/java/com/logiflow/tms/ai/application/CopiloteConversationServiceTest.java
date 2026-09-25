package com.logiflow.tms.ai.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.logiflow.tms.ai.application.command.EnvoyerMessageCopiloteCommand;
import com.logiflow.tms.ai.domain.model.InteractionIa;
import com.logiflow.tms.ai.domain.model.copilote.ContexteCopilote;
import com.logiflow.tms.ai.domain.model.copilote.EvenementCopilote;
import com.logiflow.tms.ai.domain.port.out.ContexteCopiloteStore;
import com.logiflow.tms.ai.domain.port.out.CopiloteConversationPort;
import com.logiflow.tms.ai.domain.port.out.InteractionIaRepository;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CopiloteConversationServiceTest {

  private static final UUID CONVERSATION = UUID.randomUUID();

  @Mock private CopiloteConversationPort conversationPort;
  @Mock private ContexteCopiloteStore contexteStore;
  @Mock private InteractionIaRepository interactionRepository;

  private CopiloteConversationService service;
  private final List<EvenementCopilote> recus = new ArrayList<>();

  @BeforeEach
  void setUp() {
    service =
        new CopiloteConversationService(
            conversationPort, contexteStore, interactionRepository, Duration.ofMinutes(5));
  }

  private EnvoyerMessageCopiloteCommand commande() {
    return new EnvoyerMessageCopiloteCommand(
        CONVERSATION,
        "Quels voyages sont en cours ?",
        "user-1",
        "Alice",
        Set.of("ROLE_EXPLOITANT", "ROLE_ATELIER"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void envoyerMessageEmetUnJetonDeContexteRelaieLeFluxPuisRevoqueLeJeton() {
    doAnswer(
            invocation -> {
              Consumer<EvenementCopilote> recepteur = invocation.getArgument(6);
              recepteur.accept(new EvenementCopilote("token", "{\"texte\":\"Bonjour\"}"));
              return null;
            })
        .when(conversationPort)
        .envoyerMessage(
            eq(CONVERSATION),
            eq("Quels voyages sont en cours ?"),
            eq("user-1"),
            eq("Alice"),
            eq(Set.of("EXPLOITANT", "ATELIER")),
            anyString(),
            any(Consumer.class));

    Instant avant = Instant.now();
    service.envoyerMessage(commande(), recus::add);

    ArgumentCaptor<ContexteCopilote> contexte = ArgumentCaptor.forClass(ContexteCopilote.class);
    verify(contexteStore).enregistrer(contexte.capture());
    assertThat(contexte.getValue().utilisateurId()).isEqualTo("user-1");
    assertThat(contexte.getValue().roles()).containsExactlyInAnyOrder("EXPLOITANT", "ATELIER");
    assertThat(contexte.getValue().jeton()).hasSizeGreaterThanOrEqualTo(40);
    assertThat(contexte.getValue().expireLe()).isAfter(avant.plus(Duration.ofMinutes(4)));
    verify(conversationPort)
        .envoyerMessage(
            eq(CONVERSATION),
            anyString(),
            anyString(),
            anyString(),
            any(),
            eq(contexte.getValue().jeton()),
            any(Consumer.class));
    verify(contexteStore).revoquer(contexte.getValue().jeton());
    assertThat(recus).extracting(EvenementCopilote::nom).containsExactly("token");

    ArgumentCaptor<InteractionIa> journal = ArgumentCaptor.forClass(InteractionIa.class);
    verify(interactionRepository).sauvegarder(journal.capture());
    assertThat(journal.getValue().succes()).isTrue();
  }

  @Test
  @SuppressWarnings("unchecked")
  void conversationIntrouvableDevientUnEvenementErreur() {
    doThrow(new NotFoundException("Conversation introuvable"))
        .when(conversationPort)
        .envoyerMessage(any(), any(), any(), any(), any(), any(), any(Consumer.class));

    service.envoyerMessage(commande(), recus::add);

    assertThat(recus).hasSize(1);
    assertThat(recus.getFirst().nom()).isEqualTo("erreur");
    assertThat(recus.getFirst().donnees()).contains("CONVERSATION_INTROUVABLE");
    verify(contexteStore).revoquer(anyString());
  }

  @Test
  @SuppressWarnings("unchecked")
  void serviceIaIndisponibleDevientUnEvenementErreurEtEstJournalise() {
    doThrow(new ServiceIndisponibleException("injoignable"))
        .when(conversationPort)
        .envoyerMessage(any(), any(), any(), any(), any(), any(), any(Consumer.class));

    service.envoyerMessage(commande(), recus::add);

    assertThat(recus.getFirst().donnees()).contains("SERVICE_INDISPONIBLE");
    ArgumentCaptor<InteractionIa> journal = ArgumentCaptor.forClass(InteractionIa.class);
    verify(interactionRepository).sauvegarder(journal.capture());
    assertThat(journal.getValue().succes()).isFalse();
  }

  @Test
  @SuppressWarnings("unchecked")
  void clientDeconnecteInterromptLeFluxEtRevoqueLeJeton() {
    doAnswer(
            invocation -> {
              Consumer<EvenementCopilote> recepteur = invocation.getArgument(6);
              recepteur.accept(new EvenementCopilote("token", "{}"));
              return null;
            })
        .when(conversationPort)
        .envoyerMessage(any(), any(), any(), any(), any(), any(), any(Consumer.class));

    assertThatThrownBy(
            () ->
                service.envoyerMessage(
                    commande(),
                    evenement -> {
                      throw new IllegalStateException("client parti");
                    }))
        .isInstanceOf(IllegalStateException.class);
    verify(contexteStore).revoquer(anyString());
  }
}
