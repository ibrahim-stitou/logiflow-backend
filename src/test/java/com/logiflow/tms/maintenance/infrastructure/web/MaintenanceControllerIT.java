package com.logiflow.tms.maintenance.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.infrastructure.web.dto.OrdreTravailRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.PlanEntretienRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.ScoreSanteRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.DeviseApplication;
import com.logiflow.tms.shared.domain.vo.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class MaintenanceControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private UUID creerVehicule() throws Exception {
    var requete =
        new VehiculeRequest(
            "MT-IT-001", TypeVehicule.PORTEUR, null, null, null, null, null, null, 19000, null,
            9000, null, null, null, null, null, null, false, null, null, null, null, null);
    String reponse =
        mockMvc
            .perform(
                post("/api/v1/vehicules")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requete)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return UUID.fromString(objectMapper.readTree(reponse).get("id").asText());
  }

  @Test
  void creerUnOrdreTravailPourUnVehiculeExistant() throws Exception {
    UUID vehiculeId = creerVehicule();
    var requete =
        new OrdreTravailRequest(
            vehiculeId,
            TypeIntervention.ENTRETIEN_PREVENTIF,
            LocalDateTime.now(),
            new Money(BigDecimal.valueOf(250), DeviseApplication.PAR_DEFAUT));

    mockMvc
        .perform(
            post("/api/v1/ordres-travail")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.statut").value("PLANIFIE"));
  }

  @Test
  void statsOrdresDeTravail() throws Exception {
    mockMvc
        .perform(get("/api/v1/ordres-travail/stats").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").isNumber())
        .andExpect(jsonPath("$.coutTotal.montant").isNumber())
        .andExpect(jsonPath("$.coutTotal.devise").value("MAD"))
        .andExpect(jsonPath("$.enCours").isNumber());
  }

  @Test
  void listerLesOrdresDeTravail() throws Exception {
    UUID vehiculeId = creerVehicule();
    var requete =
        new OrdreTravailRequest(
            vehiculeId,
            TypeIntervention.REPARATION,
            LocalDateTime.now(),
            new Money(BigDecimal.valueOf(180), DeviseApplication.PAR_DEFAUT));

    mockMvc
        .perform(
            post("/api/v1/ordres-travail")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/v1/ordres-travail")
                .with(jwt())
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.content[0].statut").value("PLANIFIE"));
  }

  @Test
  void listerLesOrdresDeTravailParVehicule() throws Exception {
    UUID vehiculeA = creerVehicule();
    UUID vehiculeB = creerVehicule();
    var ordreA =
        new OrdreTravailRequest(
            vehiculeA,
            TypeIntervention.ENTRETIEN_PREVENTIF,
            LocalDateTime.now(),
            new Money(BigDecimal.valueOf(120), DeviseApplication.PAR_DEFAUT));
    var ordreB =
        new OrdreTravailRequest(
            vehiculeB,
            TypeIntervention.REPARATION,
            LocalDateTime.now(),
            new Money(BigDecimal.valueOf(300), DeviseApplication.PAR_DEFAUT));

    mockMvc
        .perform(
            post("/api/v1/ordres-travail")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordreA)))
        .andExpect(status().isCreated());
    mockMvc
        .perform(
            post("/api/v1/ordres-travail")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ordreB)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/v1/ordres-travail")
                .with(jwt())
                .param("vehiculeId", vehiculeA.toString())
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].vehiculeId").value(vehiculeA.toString()));
  }

  @Test
  void listerLesPlansEntretien() throws Exception {
    UUID vehiculeId = creerVehicule();
    var requete = new PlanEntretienRequest(vehiculeId, "Vidange moteur", 30_000, 12, 500, 60);

    mockMvc
        .perform(
            post("/api/v1/plans-entretien")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/v1/plans-entretien")
                .with(jwt())
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.content[0].libelle").value("Vidange moteur"));
  }

  @Test
  void consulterLeDernierScoreDeSante() throws Exception {
    UUID vehiculeId = creerVehicule();
    var requete = new ScoreSanteRequest(vehiculeId, 92, 4000, LocalDate.now().plusMonths(3), null);

    mockMvc
        .perform(
            post("/api/v1/scores-sante")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/v1/scores-sante/dernier")
                .with(jwt())
                .param("vehiculeId", vehiculeId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("BON"));
  }

  @Test
  void calculerUnScoreDeSanteDeriveLeStatut() throws Exception {
    UUID vehiculeId = creerVehicule();
    var requete = new ScoreSanteRequest(vehiculeId, 92, 4000, LocalDate.now().plusMonths(3), null);

    mockMvc
        .perform(
            post("/api/v1/scores-sante")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.statut").value("BON"));
  }

  @Test
  void creerUnOrdreTravailPourUnVehiculeInexistantRenvoie404() throws Exception {
    var requete =
        new OrdreTravailRequest(
            UUID.randomUUID(),
            TypeIntervention.REPARATION,
            LocalDateTime.now(),
            new Money(BigDecimal.TEN, DeviseApplication.PAR_DEFAUT));

    mockMvc
        .perform(
            post("/api/v1/ordres-travail")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isNotFound());
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc
        .perform(get("/api/v1/ordres-travail/{id}", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }
}
