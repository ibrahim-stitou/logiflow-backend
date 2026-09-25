package com.logiflow.tms.driver.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    return new ChauffeurRequest(matricule, "Dupont", "Jean", null, List.of(), 2100);
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
  void accederSansAuthentificationLAccesEstRefuse() throws Exception {
    // Profil test permissif (pas de resource server JWT) : refus anonyme en 403 ; 401 en dev/prod.
    mockMvc.perform(get("/api/v1/chauffeurs")).andExpect(status().is4xxClientError());
  }

  private String creer(String corps) throws Exception {
    String reponse =
        mockMvc
            .perform(
                post("/api/v1/chauffeurs")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(corps))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(reponse).get("id").asText();
  }

  @Test
  void creerAvecProfilCompletPuisModifierToutLeProfil() throws Exception {
    String id =
        creer(
            """
            {"matricule": "ch-it-02", "nom": "Martin", "prenom": "Paul",
             "profil": {"telephone": "0600000000", "email": "paul@exemple.fr",
                        "numeroPermis": "PERM-1", "categoriesPermis": ["CE", "C"],
                        "dateExpirationPermis": "2031-05-01", "typeContrat": "CDI",
                        "siteRattachementId": "11111111-1111-4111-8111-111111111111"},
             "habilitations": [{"type": "ADR_BASE", "reference": "ADR-9",
                                "dateObtention": "2024-01-01", "dateExpiration": "2029-01-01"}],
             "soldeTempsConduiteInitialMinutes": 2700}
            """);

    mockMvc
        .perform(get("/api/v1/chauffeurs/{id}", id).with(jwt()))
        .andExpect(jsonPath("$.matricule").value("CH-IT-02"))
        .andExpect(jsonPath("$.categoriesPermis[0]").value("C"))
        .andExpect(jsonPath("$.categoriesPermis[1]").value("CE"))
        .andExpect(jsonPath("$.siteRattachementId").value("11111111-1111-4111-8111-111111111111"))
        .andExpect(jsonPath("$.habilitations[0].type").value("ADR_BASE"));

    mockMvc
        .perform(
            put("/api/v1/chauffeurs/{id}", id)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"nom": "Martin", "prenom": "Pierre",
                     "profil": {"telephone": "0611111111", "categoriesPermis": ["C"],
                                "numeroPasseport": "PA-77", "dateExpirationPasseport": "2032-01-01"},
                     "habilitations": []}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.prenom").value("Pierre"))
        .andExpect(jsonPath("$.telephone").value("0611111111"))
        .andExpect(jsonPath("$.email").doesNotExist())
        .andExpect(jsonPath("$.numeroPasseport").value("PA-77"))
        .andExpect(jsonPath("$.categoriesPermis.length()").value(1))
        .andExpect(jsonPath("$.habilitations.length()").value(0));
  }

  @Test
  void changerStatutEtDisponibilitePuisFiltrerLaListe() throws Exception {
    String id =
        creer(
            """
            {"matricule": "CH-IT-03", "nom": "Zidane", "prenom": "Filtre",
             "habilitations": [], "soldeTempsConduiteInitialMinutes": 0}
            """);

    mockMvc
        .perform(
            patch("/api/v1/chauffeurs/{id}/statut", id)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valeur\": \"SUSPENDU\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("SUSPENDU"));
    mockMvc
        .perform(
            patch("/api/v1/chauffeurs/{id}/disponibilite", id)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valeur\": \"EN_CONGE\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.disponibilite").value("EN_CONGE"));

    mockMvc
        .perform(
            get("/api/v1/chauffeurs")
                .param("q", "zidane")
                .param("statut", "SUSPENDU")
                .param("disponibilite", "EN_CONGE")
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].matricule").value("CH-IT-03"));
    mockMvc
        .perform(
            get("/api/v1/chauffeurs").param("q", "zidane").param("statut", "ACTIF").with(jwt()))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void unStatutInconnuRenvoie400() throws Exception {
    String id =
        creer(
            """
            {"matricule": "CH-IT-04", "nom": "A", "prenom": "B",
             "habilitations": [], "soldeTempsConduiteInitialMinutes": 0}
            """);
    mockMvc
        .perform(
            patch("/api/v1/chauffeurs/{id}/statut", id)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valeur\": \"RETRAITE\"}"))
        .andExpect(status().isBadRequest());
  }
}
