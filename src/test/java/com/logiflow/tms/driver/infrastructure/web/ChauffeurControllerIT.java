package com.logiflow.tms.driver.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/** Test d'intégration bout en bout du {@link ChauffeurController}. */
@AutoConfigureMockMvc
class ChauffeurControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private static ChauffeurRequest requeteMinimale(String matricule) {
    return new ChauffeurRequest(
        matricule, "Dupont", "Jean", null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        List.of(), 2100);
  }

  @Test
  void creerPuisConsulterUnChauffeur() throws Exception {
    var requete = requeteMinimale("CH-IT-01");

    String reponseCreation =
        mockMvc
            .perform(
                post("/api/v1/chauffeurs")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requete)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.matricule").value("CH-IT-01"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = objectMapper.readTree(reponseCreation).get("id").asText();

    mockMvc
        .perform(get("/api/v1/chauffeurs/{id}", id).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("ACTIF"))
        .andExpect(jsonPath("$.disponibilite").value("DISPONIBLE"));
  }

  @Test
  void creerUnChauffeurAvecUnMatriculeVideRenvoie400() throws Exception {
    String corpsInvalide =
        """
        {"matricule": "", "nom": "Dupont", "prenom": "Jean", "habilitations": [], "soldeTempsConduiteInitialMinutes": 0}
        """;

    mockMvc
        .perform(
            post("/api/v1/chauffeurs")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpsInvalide))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray());
  }

  @Test
  void consulterUnChauffeurInexistantRenvoie404() throws Exception {
    mockMvc
        .perform(get("/api/v1/chauffeurs/{id}", "00000000-0000-0000-0000-000000000099").with(jwt()))
        .andExpect(status().isNotFound());
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc.perform(get("/api/v1/chauffeurs")).andExpect(status().isUnauthorized());
  }
}
