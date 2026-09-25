package com.logiflow.tms.ai.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.ai.infrastructure.web.dto.ItineraireRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Test d'intégration du {@link ItineraireController}. Aucun service Flask ne tourne dans cet
 * environnement de test : ces tests exercent volontairement le chemin d'indisponibilité (503),
 * conforme au principe de dégradation gracieuse décrit dans docs/integration-ia.md — comme pour le
 * copilote, il n'existe pas de repli déterministe pertinent pour un calcul d'itinéraire.
 */
@AutoConfigureMockMvc
class ItineraireControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private static final List<ItineraireRequest.PointRequest> DEUX_POINTS =
      List.of(
          new ItineraireRequest.PointRequest(48.8566, 2.3522, "Paris"),
          new ItineraireRequest.PointRequest(45.7640, 4.8357, "Lyon"));

  @Test
  void calculerSansServiceIaDisponibleRenvoie503() throws Exception {
    var requete = new ItineraireRequest(DEUX_POINTS);

    mockMvc
        .perform(
            post("/api/v1/ia/itineraires/calcul")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void calculerAvecMoinsDeDeuxPointsRenvoie400() throws Exception {
    var requete = new ItineraireRequest(List.of(DEUX_POINTS.get(0)));

    mockMvc
        .perform(
            post("/api/v1/ia/itineraires/calcul")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void calculerGeometrieSansOsrmDisponibleReplieSurDesSegmentsDroits() throws Exception {
    var requete = new ItineraireRequest(DEUX_POINTS);

    // Sans moteur de routage, la carte trace des segments droits entre les points plutôt que
    // de ne rien afficher.
    mockMvc
        .perform(
            post("/api/v1/ia/itineraires/geometrie")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isOk())
        // Segment densifié (points intermédiaires) de Paris à Lyon.
        .andExpect(jsonPath("$.geometrie[0].latitude").value(48.8566))
        .andExpect(jsonPath("$.geometrie[-1].latitude").value(45.764))
        .andExpect(jsonPath("$.geometrie.length()").value(org.hamcrest.Matchers.greaterThan(2)));
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    var requete = new ItineraireRequest(DEUX_POINTS);

    mockMvc
        .perform(
            post("/api/v1/ia/itineraires/calcul")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isUnauthorized());
  }
}
