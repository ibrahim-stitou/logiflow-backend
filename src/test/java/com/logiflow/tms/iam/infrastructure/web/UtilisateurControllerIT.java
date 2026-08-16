package com.logiflow.tms.iam.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.iam.domain.model.RoleUtilisateur;
import com.logiflow.tms.iam.infrastructure.web.dto.UtilisateurRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/** Test d'intégration bout en bout du {@link UtilisateurController}. */
@AutoConfigureMockMvc
class UtilisateurControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void creerPuisConsulterUnUtilisateur() throws Exception {
    var requete =
        new UtilisateurRequest(
            "jean.it", "jean.it@logiflow.tms", Set.of(RoleUtilisateur.EXPLOITANT));

    String reponseCreation =
        mockMvc
            .perform(
                post("/api/v1/utilisateurs")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requete)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.login").value("jean.it"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = objectMapper.readTree(reponseCreation).get("id").asText();

    mockMvc
        .perform(get("/api/v1/utilisateurs/{id}", id).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.actif").value(true));
  }

  @Test
  void creerUnUtilisateurAvecUnEmailInvalideRenvoie400() throws Exception {
    String corpsInvalide =
        """
        {"login": "jean", "email": "pas-un-email", "roles": ["EXPLOITANT"]}
        """;

    mockMvc
        .perform(
            post("/api/v1/utilisateurs")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpsInvalide))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray());
  }

  @Test
  void consulterUnUtilisateurInexistantRenvoie404() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/utilisateurs/{id}", "00000000-0000-0000-0000-000000000099").with(jwt()))
        .andExpect(status().isNotFound());
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc.perform(get("/api/v1/utilisateurs")).andExpect(status().isUnauthorized());
  }
}
