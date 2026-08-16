package com.logiflow.tms.maintenance.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.infrastructure.web.dto.OrdreTravailRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.ScoreSanteRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.vo.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
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
    var requete = new VehiculeRequest("MT-IT-001", TypeVehicule.PORTEUR, 19000, 9000, List.of());
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
            new Money(BigDecimal.valueOf(250), Currency.getInstance("EUR")));

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
            new Money(BigDecimal.TEN, Currency.getInstance("EUR")));

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
