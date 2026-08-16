package com.logiflow.tms.referential.infrastructure.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.referential.infrastructure.web.dto.SiteRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Test d'intégration bout en bout du {@link SiteController} : base de données réelle
 * (Testcontainers) via {@link AbstractIntegrationTest}, sécurité JWT simulée via
 * spring-security-test.
 */
@AutoConfigureMockMvc
class SiteControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void creerPuisConsulterUnSite() throws Exception {
    SiteRequest requete =
        new SiteRequest(
            "SITE-IT-01",
            "Entrepôt de test",
            null,
            new GeoPoint(48.8566, 2.3522),
            null,
            null,
            null);

    String reponseCreation =
        mockMvc
            .perform(
                post("/api/v1/sites")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requete)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("SITE-IT-01"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = objectMapper.readTree(reponseCreation).get("id").asText();

    mockMvc
        .perform(get("/api/v1/sites/{id}", id).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.libelle").value("Entrepôt de test"));
  }

  @Test
  void listerLesSitesEstPagine() throws Exception {
    for (int i = 0; i < 3; i++) {
      SiteRequest requete =
          new SiteRequest(
              "SITE-IT-PAGE-" + i,
              "Entrepôt " + i,
              null,
              new GeoPoint(48.85, 2.35),
              null,
              null,
              null);
      mockMvc
          .perform(
              post("/api/v1/sites")
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requete)))
          .andExpect(status().isCreated());
    }

    mockMvc
        .perform(get("/api/v1/sites").with(jwt()).param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(3)))
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void creerUnSiteAvecUnCorpsInvalideRenvoie400AvecLesViolations() throws Exception {
    String corpsInvalide =
        """
        {"code": "", "libelle": "", "localisation": null}
        """;

    mockMvc
        .perform(
            post("/api/v1/sites")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpsInvalide))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.violations").isArray());
  }

  @Test
  void consulterUnSiteInexistantRenvoie404() throws Exception {
    mockMvc
        .perform(get("/api/v1/sites/{id}", "00000000-0000-0000-0000-000000000099").with(jwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc.perform(get("/api/v1/sites")).andExpect(status().isUnauthorized());
  }
}
