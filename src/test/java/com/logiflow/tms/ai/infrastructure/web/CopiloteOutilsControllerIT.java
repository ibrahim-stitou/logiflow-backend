package com.logiflow.tms.ai.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.ai.domain.model.copilote.ContexteCopilote;
import com.logiflow.tms.ai.domain.port.out.ContexteCopiloteStore;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import java.time.Instant;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Outils du copilote rappelés par le service IA : authentification par clé de rappel + jeton de
 * contexte, filtrage par rôle, validation des arguments renvoyée au LLM.
 */
@AutoConfigureMockMvc
class CopiloteOutilsControllerIT extends AbstractIntegrationTest {

  private static final String CLE = "local-dev-callback-key";

  @Autowired private MockMvc mockMvc;
  @Autowired private ContexteCopiloteStore contexteStore;

  private String jeton(String... roles) {
    String jeton = "jeton-" + String.join("-", roles) + "-" + System.nanoTime();
    contexteStore.enregistrer(
        new ContexteCopilote(
            jeton, "user-1", "Alice", Set.of(roles), Instant.now().plusSeconds(60)));
    return jeton;
  }

  @Test
  void sansCleDeRappelRenvoie401() throws Exception {
    mockMvc
        .perform(
            get("/internal/copilote/outils").header("X-Copilote-Contexte", jeton("EXPLOITANT")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void avecUneMauvaiseCleRenvoie401() throws Exception {
    mockMvc
        .perform(
            get("/internal/copilote/outils")
                .header("X-Internal-Api-Key", "mauvaise-cle")
                .header("X-Copilote-Contexte", jeton("EXPLOITANT")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void avecUnJetonInconnuRenvoie401() throws Exception {
    mockMvc
        .perform(
            get("/internal/copilote/outils")
                .header("X-Internal-Api-Key", CLE)
                .header("X-Copilote-Contexte", "jeton-invente"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void unJwtUtilisateurNOuvrePasLesOutilsInternes() throws Exception {
    mockMvc
        .perform(get("/internal/copilote/outils").with(jwt()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void leCatalogueEstFiltreSelonLesRolesDuJeton() throws Exception {
    mockMvc
        .perform(
            get("/internal/copilote/outils")
                .header("X-Internal-Api-Key", CLE)
                .header("X-Copilote-Contexte", jeton("ATELIER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].nom", Matchers.hasItem("lister_vehicules")))
        .andExpect(jsonPath("$[*].nom", Matchers.not(Matchers.hasItem("rechercher_dossiers"))))
        .andExpect(jsonPath("$[0].parametres.type").value("object"));
  }

  @Test
  void executerUnOutilAutoriseRenvoieResultatsTotalEtSources() throws Exception {
    mockMvc
        .perform(
            post("/internal/copilote/outils/rechercher_dossiers")
                .header("X-Internal-Api-Key", CLE)
                .header("X-Copilote-Contexte", jeton("COMMERCIAL"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"statut\": \"EN_TRANSIT\", \"limite\": 5}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.resultats").isArray())
        .andExpect(jsonPath("$.total").isNumber())
        .andExpect(jsonPath("$.sources").isArray());
  }

  @Test
  void chaqueOutilDuCatalogueSExecuteSansArguments() throws Exception {
    String jeton = jeton("ADMINISTRATEUR");
    for (String outil :
        new String[] {
          "rechercher_dossiers",
          "rechercher_voyages",
          "rechercher_commandes",
          "rechercher_clients",
          "lister_vehicules",
          "lister_remorques",
          "rechercher_chauffeurs",
          "consulter_maintenance",
          "consommation_carburant"
        }) {
      mockMvc
          .perform(
              post("/internal/copilote/outils/" + outil)
                  .header("X-Internal-Api-Key", CLE)
                  .header("X-Copilote-Contexte", jeton)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{}"))
          .andExpect(status().isOk());
    }
  }

  @Test
  void unOutilHorsDroitsRenvoie403() throws Exception {
    mockMvc
        .perform(
            post("/internal/copilote/outils/rechercher_dossiers")
                .header("X-Internal-Api-Key", CLE)
                .header("X-Copilote-Contexte", jeton("ATELIER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void unArgumentInvalideRenvoie400AvecUnMessageExploitableParLeLlm() throws Exception {
    mockMvc
        .perform(
            post("/internal/copilote/outils/rechercher_dossiers")
                .header("X-Internal-Api-Key", CLE)
                .header("X-Copilote-Contexte", jeton("EXPLOITANT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"statut\": \"PERDU\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail", Matchers.containsString("valeurs possibles")));
  }

  @Test
  void unOutilInconnuRenvoie404() throws Exception {
    mockMvc
        .perform(
            post("/internal/copilote/outils/supprimer_tout")
                .header("X-Internal-Api-Key", CLE)
                .header("X-Copilote-Contexte", jeton("EXPLOITANT"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isNotFound());
  }
}
