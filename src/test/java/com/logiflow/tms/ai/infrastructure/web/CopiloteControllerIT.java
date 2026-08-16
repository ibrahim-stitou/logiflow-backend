package com.logiflow.tms.ai.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.ai.infrastructure.web.dto.CopiloteRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Test d'intégration du {@link CopiloteController}. Aucun service Flask ne tourne dans cet
 * environnement de test : ces tests exercent volontairement le chemin d'indisponibilité (503),
 * conforme au principe de dégradation gracieuse décrit dans docs/integration-ia.md.
 */
@AutoConfigureMockMvc
class CopiloteControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void poserUneQuestionSansServiceIaDisponibleRenvoie503() throws Exception {
    var requete = new CopiloteRequest("Quels camions sont libres demain ?");

    mockMvc
        .perform(
            post("/api/v1/ia/copilote/questions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void poserUneQuestionVideRenvoie400() throws Exception {
    String corpsInvalide =
        """
        {"question": ""}
        """;

    mockMvc
        .perform(
            post("/api/v1/ia/copilote/questions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpsInvalide))
        .andExpect(status().isBadRequest());
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    var requete = new CopiloteRequest("Question");

    mockMvc
        .perform(
            post("/api/v1/ia/copilote/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isUnauthorized());
  }
}
