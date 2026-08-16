package com.logiflow.tms.order.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.order.infrastructure.web.dto.CommandeRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.ClientRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.vo.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/** Test d'intégration bout en bout du {@link CommandeController}. */
@AutoConfigureMockMvc
class CommandeControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void creerPuisConfirmerUneCommande() throws Exception {
    var clientRequest = new ClientRequest("CLI-IT-ORDER-01", "Client de test commande");
    String reponseClient =
        mockMvc
            .perform(
                post("/api/v1/clients")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(clientRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String clientId = objectMapper.readTree(reponseClient).get("id").asText();

    var commandeRequest =
        new CommandeRequest(
            java.util.UUID.fromString(clientId),
            LocalDate.now().plusDays(3),
            new Money(BigDecimal.valueOf(1500), Currency.getInstance("EUR")));

    String reponseCommande =
        mockMvc
            .perform(
                post("/api/v1/commandes")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commandeRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.statut").value("RECUE"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String commandeId = objectMapper.readTree(reponseCommande).get("id").asText();

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/v1/commandes/{id}/confirmer", commandeId)
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("CONFIRMEE"));
  }

  @Test
  void creerUneCommandeAvecUnClientInexistantRenvoie404() throws Exception {
    var requete =
        new CommandeRequest(
            java.util.UUID.randomUUID(),
            LocalDate.now().plusDays(3),
            new Money(BigDecimal.TEN, Currency.getInstance("EUR")));

    mockMvc
        .perform(
            post("/api/v1/commandes")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isNotFound());
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc.perform(get("/api/v1/commandes")).andExpect(status().isUnauthorized());
  }
}
