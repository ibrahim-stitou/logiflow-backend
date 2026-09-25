package com.logiflow.tms.ai.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.logiflow.tms.ai.domain.model.copilote.EvenementCopilote;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CopiloteConversationHttpAdapterTest {

  @Test
  void lireFluxDecoupeLesEvenementsSse() throws Exception {
    String flux =
        """
        event: meta
        data: {"conversationId":"c1","messageId":"m1"}

        event: token
        data: {"texte":"Bonjour é"}

        : commentaire ignoré
        event: fin
        data: {"messageId":"m1"}""";
    List<EvenementCopilote> recus = new ArrayList<>();

    CopiloteConversationHttpAdapter.lireFlux(
        new ByteArrayInputStream(flux.getBytes(StandardCharsets.UTF_8)), recus::add);

    assertThat(recus)
        .containsExactly(
            new EvenementCopilote("meta", "{\"conversationId\":\"c1\",\"messageId\":\"m1\"}"),
            new EvenementCopilote("token", "{\"texte\":\"Bonjour é\"}"),
            new EvenementCopilote("fin", "{\"messageId\":\"m1\"}"));
  }
}
