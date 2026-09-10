package com.logiflow.tms.dossier.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.dossier.domain.model.TypeSegment;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import com.logiflow.tms.dossier.infrastructure.web.dto.DossierRequest;
import com.logiflow.tms.order.domain.vo.LigneCommande;
import com.logiflow.tms.order.infrastructure.web.dto.CommandeRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.ClientRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.MarchandiseRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.TimeWindow;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Test d'intégration bout en bout du {@link DossierController}, chaîne complète client -> commande
 * -> dossier.
 */
@AutoConfigureMockMvc
class DossierControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private UUID creerMarchandiseId(String code) throws Exception {
    var requete = new MarchandiseRequest(code, "Marchandise de test", null, null, null, true);
    String reponse =
        mockMvc
            .perform(
                post("/api/v1/marchandises")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requete)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return UUID.fromString(objectMapper.readTree(reponse).get("id").asText());
  }

  private UUID creerCommandeConfirmee(UUID marchandiseId) throws Exception {
    var clientRequest = new ClientRequest("CLI-IT-DOSSIER-01", "Client de test dossier");
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
    UUID clientId = UUID.fromString(objectMapper.readTree(reponseClient).get("id").asText());

    var commandeRequest =
        new CommandeRequest(
            clientId,
            LocalDate.now().plusDays(3),
            new Money(BigDecimal.valueOf(1500), Currency.getInstance("EUR")),
            List.of(new LigneCommande(marchandiseId, 500, 2.5, 10)));
    String reponseCommande =
        mockMvc
            .perform(
                post("/api/v1/commandes")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commandeRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID commandeId = UUID.fromString(objectMapper.readTree(reponseCommande).get("id").asText());

    mockMvc
        .perform(put("/api/v1/commandes/{id}/confirmer", commandeId).with(jwt()))
        .andExpect(status().isOk());
    return commandeId;
  }

  @Test
  void creerPuisConsulterUnDossierPourUneCommandeConfirmee() throws Exception {
    UUID marchandiseId = creerMarchandiseId("MARCH-IT-DOSSIER-01");
    UUID commandeId = creerCommandeConfirmee(marchandiseId);
    Instant maintenant = Instant.now();

    var dossierRequest =
        new DossierRequest(
            commandeId,
            TypeTransport.NATIONAL,
            true,
            10,
            "Palettes standard",
            null,
            null,
            List.of(new LigneMarchandise(marchandiseId, 500, 2.5, 10, null, null, true)),
            List.of(
                new Segment(
                    TypeSegment.CHARGEMENT,
                    0,
                    UUID.randomUUID(),
                    new TimeWindow(maintenant, maintenant.plus(2, ChronoUnit.HOURS)),
                    null),
                new Segment(
                    TypeSegment.DECHARGEMENT,
                    1,
                    UUID.randomUUID(),
                    new TimeWindow(
                        maintenant.plus(1, ChronoUnit.DAYS),
                        maintenant.plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)),
                    null)),
            List.of());

    String reponse =
        mockMvc
            .perform(
                post("/api/v1/dossiers")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dossierRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.statut").value("CREE"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String dossierId = objectMapper.readTree(reponse).get("id").asText();

    mockMvc
        .perform(
            put("/api/v1/dossiers/{id}/statut", dossierId).with(jwt()).param("valeur", "PLANIFIE"))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void creerUnDossierPourUneCommandeNonConfirmeeEchoueAvec422() throws Exception {
    UUID marchandiseId = creerMarchandiseId("MARCH-IT-DOSSIER-02");
    var clientRequest = new ClientRequest("CLI-IT-DOSSIER-02", "Client dossier non confirme");
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
    UUID clientId = UUID.fromString(objectMapper.readTree(reponseClient).get("id").asText());

    var commandeRequest =
        new CommandeRequest(
            clientId,
            LocalDate.now().plusDays(3),
            new Money(BigDecimal.TEN, Currency.getInstance("EUR")),
            List.of(new LigneCommande(marchandiseId, 500, 2.5, 10)));
    String reponseCommande =
        mockMvc
            .perform(
                post("/api/v1/commandes")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commandeRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    UUID commandeId = UUID.fromString(objectMapper.readTree(reponseCommande).get("id").asText());

    Instant maintenant = Instant.now();
    var dossierRequest =
        new DossierRequest(
            commandeId,
            TypeTransport.NATIONAL,
            true,
            10,
            "Palettes",
            null,
            null,
            List.of(new LigneMarchandise(marchandiseId, 500, 2.5, 10, null, null, true)),
            List.of(
                new Segment(
                    TypeSegment.CHARGEMENT,
                    0,
                    UUID.randomUUID(),
                    new TimeWindow(maintenant, maintenant.plus(2, ChronoUnit.HOURS)),
                    null),
                new Segment(
                    TypeSegment.DECHARGEMENT,
                    1,
                    UUID.randomUUID(),
                    new TimeWindow(
                        maintenant.plus(1, ChronoUnit.DAYS),
                        maintenant.plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)),
                    null)),
            List.of());

    mockMvc
        .perform(
            post("/api/v1/dossiers")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dossierRequest)))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc.perform(get("/api/v1/dossiers")).andExpect(status().isUnauthorized());
  }
}
