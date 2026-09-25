package com.logiflow.tms.maintenance.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.ScoreSanteRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Test d'intégration du module maintenance refondu : cycle d'un OT jusqu'à la clôture (statut de la
 * flotte, compteurs, plan), sinistre avec contrat d'assurance et OT de réparation, coûts.
 */
@AutoConfigureMockMvc
class MaintenanceControllerIT extends AbstractIntegrationTest {

  private static final LocalDateTime DEBUT = LocalDateTime.now().withNano(0).minusDays(1);

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private ResultActions envoyer(
      org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requete,
      Object corps)
      throws Exception {
    return mockMvc.perform(
        requete
            .with(jwt())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(corps)));
  }

  private JsonNode json(ResultActions resultat) throws Exception {
    return objectMapper.readTree(resultat.andReturn().getResponse().getContentAsString());
  }

  private UUID creer(String uri, Object corps) throws Exception {
    return UUID.fromString(
        json(envoyer(post(uri), corps).andExpect(status().isCreated())).get("id").asText());
  }

  private UUID creerVehicule() throws Exception {
    return creer(
        "/api/v1/vehicules",
        new VehiculeRequest(
            "MT-" + (100 + (int) (Math.random() * 900)) + "-IT",
            TypeVehicule.PORTEUR,
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
            null));
  }

  private static Map<String, Object> details(String titre) {
    return Map.of(
        "type",
        "ENTRETIEN_PREVENTIF",
        "nature",
        "PREVENTIF",
        "titre",
        titre,
        "debutPlanifie",
        DEBUT.toString(),
        "finPlanifiee",
        DEBUT.plusHours(4).toString(),
        "budgetEstime",
        300);
  }

  private static List<Map<String, Object>> lignes() {
    return List.of(
        Map.of(
            "type",
            "MAIN_OEUVRE",
            "designation",
            "Main-d'œuvre",
            "quantite",
            2,
            "prixUnitaireHt",
            65),
        Map.of(
            "type",
            "PIECE",
            "designation",
            "Filtre à huile",
            "referencePiece",
            "FH-12",
            "quantite",
            1,
            "prixUnitaireHt",
            40,
            "tauxTva",
            20));
  }

  @Test
  void cycleDUnOtIssuDUnPlanJusquALaCloture() throws Exception {
    UUID vehiculeId = creerVehicule();
    UUID planId =
        creer(
            "/api/v1/maintenance/plans",
            Map.of(
                "engin", Map.of("type", "VEHICULE", "id", vehiculeId),
                "parametres",
                    Map.of(
                        "libelle",
                        "Révision",
                        "periodiciteKm",
                        40000,
                        "periodiciteMois",
                        12,
                        "seuilAlerteKm",
                        2000,
                        "seuilAlerteJours",
                        15,
                        "dureeEstimeeMin",
                        240)));

    String otJson =
        envoyer(
                post("/api/v1/maintenance/ordres-travail"),
                Map.of(
                    "engin", Map.of("type", "VEHICULE", "id", vehiculeId),
                    "origine", "PLAN_ENTRETIEN",
                    "planId", planId,
                    "details", details("Révision annuelle"),
                    "lignes", lignes()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reference").value(org.hamcrest.Matchers.startsWith("OT-")))
            .andExpect(jsonPath("$.statut").value("PLANIFIE"))
            .andExpect(jsonPath("$.totalHt").value(170.0))
            .andExpect(jsonPath("$.totalTtc").value(204.0))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String otId = objectMapper.readTree(otJson).get("id").asText();

    envoyer(
            patch("/api/v1/maintenance/ordres-travail/{id}/statut", otId),
            Map.of("valeur", "EN_COURS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.debutReel").exists());
    mockMvc
        .perform(get("/api/v1/vehicules/{id}", vehiculeId).with(jwt()))
        .andExpect(jsonPath("$.statut").value("EN_MAINTENANCE"));

    envoyer(
            post("/api/v1/maintenance/ordres-travail/{id}/cloture", otId),
            Map.of(
                "finReelle",
                LocalDateTime.now(java.time.ZoneId.of("Europe/Paris"))
                    .plusMinutes(5)
                    .withNano(0)
                    .toString(),
                "kilometrage",
                12345,
                "travauxRealises",
                "Vidange et filtres",
                "numeroFacture",
                "FAC-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("TERMINE"));

    mockMvc
        .perform(get("/api/v1/vehicules/{id}", vehiculeId).with(jwt()))
        .andExpect(jsonPath("$.statut").value("DISPONIBLE"))
        .andExpect(jsonPath("$.kilometrage").value(12345));
    mockMvc
        .perform(get("/api/v1/maintenance/plans/{id}", planId).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.derniereKm").value(12345))
        .andExpect(jsonPath("$.echeance.kmRestant").value(40000))
        .andExpect(jsonPath("$.echeance.etat").value("OK"));
    mockMvc
        .perform(get("/api/v1/maintenance/plans/{id}/ordres-travail", planId).with(jwt()))
        .andExpect(jsonPath("$.length()").value(1));
    mockMvc
        .perform(
            get("/api/v1/maintenance/couts").param("enginId", vehiculeId.toString()).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombreOrdres").value(1))
        .andExpect(jsonPath("$.totalHt").value(170.0))
        .andExpect(jsonPath("$.parNature[0].cle").value("PREVENTIF"));
  }

  @Test
  void sinistreAvecContratDAssuranceEtReparation() throws Exception {
    UUID vehiculeId = creerVehicule();
    UUID assureurId =
        creer(
            "/api/v1/maintenance/prestataires",
            Map.of(
                "code",
                "ASS-IT-" + vehiculeId.toString().substring(0, 6),
                "raisonSociale",
                "Assureur IT",
                "type",
                "ASSUREUR"));
    // Contrat dédié : il prime sur le contrat de flotte des données de démonstration.
    creer(
        "/api/v1/maintenance/contrats-assurance",
        Map.of(
            "assureurId",
            assureurId,
            "numeroPolice",
            "POL-IT",
            "type",
            "ENGIN",
            "engins",
            List.of(Map.of("type", "VEHICULE", "id", vehiculeId)),
            "garanties",
            List.of("RC", "DOMMAGES"),
            "franchise",
            500,
            "dateEffet",
            LocalDate.now().minusMonths(1).toString(),
            "dateEcheance",
            LocalDate.now().plusMonths(11).toString()));

    String sinistreJson =
        envoyer(
                post("/api/v1/maintenance/sinistres"),
                Map.of(
                    "vehiculeId",
                    vehiculeId,
                    "dateSurvenance",
                    DEBUT.toString(),
                    "type",
                    "ACCROCHAGE",
                    "gravite",
                    "MATERIEL_LEGER",
                    "description",
                    "Accrochage sur un quai de chargement",
                    "enginImmobilise",
                    true))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reference").value(org.hamcrest.Matchers.startsWith("SIN-")))
            .andExpect(jsonPath("$.franchise").value(500.0))
            .andExpect(jsonPath("$.contratId").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String sinistreId = objectMapper.readTree(sinistreJson).get("id").asText();
    mockMvc
        .perform(get("/api/v1/vehicules/{id}", vehiculeId).with(jwt()))
        .andExpect(jsonPath("$.statut").value("IMMOBILISE"));

    String otJson =
        envoyer(
                post("/api/v1/maintenance/sinistres/{id}/reparations", sinistreId),
                Map.of(
                    "engin", Map.of("type", "VEHICULE", "id", vehiculeId),
                    "details",
                        Map.of(
                            "type", "CARROSSERIE",
                            "nature", "CORRECTIF",
                            "titre", "Reprise carrosserie",
                            "debutPlanifie", DEBUT.plusHours(2).toString())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.origine").value("SINISTRE"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String otId = objectMapper.readTree(otJson).get("id").asText();

    envoyer(
            patch("/api/v1/maintenance/sinistres/{id}/statut", sinistreId),
            Map.of("valeur", "CLOS"))
        .andExpect(status().is4xxClientError());

    envoyer(put("/api/v1/maintenance/ordres-travail/{id}/lignes", otId), lignes())
        .andExpect(status().isOk());
    envoyer(
            patch("/api/v1/maintenance/ordres-travail/{id}/statut", otId),
            Map.of("valeur", "EN_COURS"))
        .andExpect(status().isOk());
    envoyer(
            post("/api/v1/maintenance/ordres-travail/{id}/cloture", otId),
            Map.of(
                "finReelle",
                LocalDateTime.now(java.time.ZoneId.of("Europe/Paris"))
                    .plusMinutes(5)
                    .withNano(0)
                    .toString(),
                "kilometrage",
                5000))
        .andExpect(status().isOk());
    // Le sinistre immobilise encore le véhicule tant qu'il n'est pas clos.
    mockMvc
        .perform(get("/api/v1/vehicules/{id}", vehiculeId).with(jwt()))
        .andExpect(jsonPath("$.statut").value("EN_MAINTENANCE"));

    Map<String, Object> modification = new java.util.HashMap<>();
    modification.put("vehiculeId", vehiculeId);
    modification.put("dateSurvenance", DEBUT.toString());
    modification.put("type", "ACCROCHAGE");
    modification.put("gravite", "MATERIEL_LEGER");
    modification.put("description", "Accrochage sur un quai de chargement");
    modification.put("enginImmobilise", true);
    modification.put("indemnite", 100);
    envoyer(put("/api/v1/maintenance/sinistres/{id}", sinistreId), modification)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.couts.reparationsHt").value(170.0))
        .andExpect(jsonPath("$.couts.coutNet").value(70.0));

    envoyer(
            patch("/api/v1/maintenance/sinistres/{id}/statut", sinistreId),
            Map.of("valeur", "CLOS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("CLOS"));
    mockMvc
        .perform(get("/api/v1/vehicules/{id}", vehiculeId).with(jwt()))
        .andExpect(jsonPath("$.statut").value("DISPONIBLE"));
  }

  @Test
  void scoreDeSanteToujoursCalculable() throws Exception {
    UUID vehiculeId = creerVehicule();
    envoyer(
            post("/api/v1/scores-sante"),
            new ScoreSanteRequest(vehiculeId, 92, 4000, LocalDate.now().plusMonths(3), null))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.statut").value("BON"));
  }
}
