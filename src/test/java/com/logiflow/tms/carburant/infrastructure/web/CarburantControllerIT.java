package com.logiflow.tms.carburant.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.carburant.domain.model.TypeCarburant;
import com.logiflow.tms.carburant.infrastructure.web.dto.PriseCarburantRequest;
import com.logiflow.tms.carburant.infrastructure.web.dto.StationRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class CarburantControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void creerStationEtPriseCarburant() throws Exception {
    StationRequest stationRequest = new StationRequest("STA-IT-01", "Station test", "1 rue Test");
    String stationJson =
        mockMvc
            .perform(
                post("/api/v1/stations")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(stationRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("STA-IT-01"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID stationId = UUID.fromString(objectMapper.readTree(stationJson).get("id").asText());

    JsonNode voyages =
        objectMapper.readTree(
            mockMvc
                .perform(get("/api/v1/voyages").with(jwt()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    if (voyages.get("content").isEmpty()) {
      return;
    }
    JsonNode voyage = voyages.get("content").get(0);
    UUID voyageId = UUID.fromString(voyage.get("id").asText());
    UUID vehiculeId = UUID.fromString(voyage.get("vehiculeId").asText());

    PriseCarburantRequest priseRequest =
        new PriseCarburantRequest(
            voyageId,
            vehiculeId,
            null,
            stationId,
            TypeCarburant.DIESEL,
            120.5,
            BigDecimal.valueOf(245.80),
            Instant.now());

    String priseJson =
        mockMvc
            .perform(
                post("/api/v1/prises-carburant")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(priseRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.statut").value("BROUILLON"))
            .andExpect(jsonPath("$.litrage").value(120.5))
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID priseId = UUID.fromString(objectMapper.readTree(priseJson).get("id").asText());

    mockMvc
        .perform(post("/api/v1/prises-carburant/{id}/valider", priseId).with(jwtExploitant()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("VALIDEE"));
  }

  private static RequestPostProcessor jwtExploitant() {
    return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRATEUR"));
  }
}
