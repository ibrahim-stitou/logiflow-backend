package com.logiflow.tms.fleet.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/** Test d'intégration bout en bout du {@link VehiculeController}. */
@AutoConfigureMockMvc
class VehiculeControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void creerPuisConsulterUnVehicule() throws Exception {
    var requete =
        new VehiculeRequest(
            "XY-999-ZZ",
            com.logiflow.tms.fleet.domain.model.TypeVehicule.TRACTEUR,
            19000,
            9000,
            List.of());

    String reponseCreation =
        mockMvc
            .perform(
                post("/api/v1/vehicules")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requete)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.immatriculation").value("XY-999-ZZ"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = objectMapper.readTree(reponseCreation).get("id").asText();

    mockMvc
        .perform(get("/api/v1/vehicules/{id}", id).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("DISPONIBLE"));
  }

  @Test
  void creerUnVehiculeAvecUneImmatriculationVideRenvoie400() throws Exception {
    String corpsInvalide =
        """
        {"immatriculation": "", "type": "TRACTEUR", "ptacKg": 19000, "chargeUtileKg": 9000, "documents": []}
        """;

    mockMvc
        .perform(
            post("/api/v1/vehicules")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpsInvalide))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray());
  }

  @Test
  void consulterUnVehiculeInexistantRenvoie404() throws Exception {
    mockMvc
        .perform(get("/api/v1/vehicules/{id}", "00000000-0000-0000-0000-000000000099").with(jwt()))
        .andExpect(status().isNotFound());
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc.perform(get("/api/v1/vehicules")).andExpect(status().isUnauthorized());
  }
}
