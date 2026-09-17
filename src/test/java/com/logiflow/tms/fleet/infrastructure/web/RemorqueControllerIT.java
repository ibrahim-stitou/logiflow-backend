package com.logiflow.tms.fleet.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.fleet.domain.model.TypeCarrosserie;
import com.logiflow.tms.fleet.infrastructure.web.dto.RemorqueRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/** Test d'intégration bout en bout du {@link RemorqueController}. */
@AutoConfigureMockMvc
class RemorqueControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private static RemorqueRequest requeteMinimale(String immatriculation) {
    return new RemorqueRequest(
        immatriculation,
        null,
        TypeCarrosserie.TAUTLINER,
        null,
        null,
        null,
        null,
        null,
        null,
        80,
        33,
        24000,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        null,
        null);
  }

  @Test
  void listerLesRemorquesApresCreation() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/remorques")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requeteMinimale("RM-001-TM"))))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/remorques").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].immatriculation").value("RM-001-TM"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc.perform(get("/api/v1/remorques")).andExpect(status().isUnauthorized());
  }
}
