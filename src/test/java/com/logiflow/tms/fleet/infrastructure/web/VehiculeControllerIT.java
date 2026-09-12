package com.logiflow.tms.fleet.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
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

  private static VehiculeRequest requeteMinimale(String immatriculation, TypeVehicule type) {
    return new VehiculeRequest(
        immatriculation,
        type,
        null,
        null,
        null,
        null,
        null,
        null,
        19000,
        null,
        9000,
        null,
        null,
        null,
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
  void creerPuisConsulterUnVehicule() throws Exception {
    var requete = requeteMinimale("XY-999-ZZ", TypeVehicule.TRACTEUR);

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
  void releverCompteursApresCreation() throws Exception {
    var creation = requeteMinimale("LF-441-TM", TypeVehicule.TRACTEUR);
    String id =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        post("/api/v1/vehicules")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(creation)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("id")
            .asText();

    mockMvc
        .perform(
            put("/api/v1/vehicules/{id}/compteurs", id)
                .with(jwt())
                .param("kilometrage", "1500")
                .param("heuresMoteur", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.kilometrage").value(1500))
        .andExpect(jsonPath("$.heuresMoteur").value(10));
  }

  @Test
  void creerUnVehiculeAvecUneImmatriculationVideRenvoie400() throws Exception {
    String corpsInvalide =
        """
        {"immatriculation": "", "type": "TRACTEUR", "ptacKg": 19000, "chargeUtileKg": 9000}
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
