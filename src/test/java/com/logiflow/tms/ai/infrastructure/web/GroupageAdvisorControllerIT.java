package com.logiflow.tms.ai.infrastructure.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.ai.infrastructure.web.dto.GroupageAnalyseRequest;
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
 * Test d'intégration du {@link GroupageAdvisorController}. Aucun service Flask ne tourne dans cet
 * environnement de test : ce test vérifie que le repli déterministe fonctionne malgré tout — le
 * flux métier de groupage n'est jamais bloqué par une panne du service IA.
 */
@AutoConfigureMockMvc
class GroupageAdvisorControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private UUID creerId(String json, String uri) throws Exception {
    String reponse =
        mockMvc
            .perform(post(uri).with(jwt()).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return UUID.fromString(objectMapper.readTree(reponse).get("id").asText());
  }

  private UUID creerDossierGroupable(String suffixeClient) throws Exception {
    UUID clientId =
        creerId(
            objectMapper.writeValueAsString(
                new ClientRequest("CLI-IT-IA-" + suffixeClient, "Client IA")),
            "/api/v1/clients");
    UUID marchandiseId =
        creerId(
            objectMapper.writeValueAsString(
                new MarchandiseRequest(
                    "MARCH-IT-IA-" + suffixeClient, "Marchandise IA", null, null, null, true)),
            "/api/v1/marchandises");
    UUID commandeId =
        creerId(
            objectMapper.writeValueAsString(
                new CommandeRequest(
                    clientId,
                    LocalDate.now().plusDays(3),
                    new Money(BigDecimal.valueOf(1000), Currency.getInstance("EUR")),
                    List.of(new LigneCommande(marchandiseId, 500, 2.5, 10)))),
            "/api/v1/commandes");
    mockMvc
        .perform(put("/api/v1/commandes/{id}/confirmer", commandeId).with(jwt()))
        .andExpect(status().isOk());

    Instant maintenant = Instant.now();
    return creerId(
        objectMapper.writeValueAsString(
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
                List.of())),
        "/api/v1/dossiers");
  }

  @Test
  void analyserSansServiceIaDisponibleRenvoieUnRepliDeterministe() throws Exception {
    UUID dossier1 = creerDossierGroupable("01");
    UUID dossier2 = creerDossierGroupable("02");
    var requete = new GroupageAnalyseRequest(List.of(dossier1, dossier2));

    mockMvc
        .perform(
            post("/api/v1/ia/groupage/propositions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].genereParIa").value(false))
        .andExpect(
            jsonPath(
                "$[0].dossierIds",
                org.hamcrest.Matchers.containsInAnyOrder(
                    dossier1.toString(), dossier2.toString())));
  }

  @Test
  void analyserAvecUnDossierInexistantRenvoie404() throws Exception {
    var requete = new GroupageAnalyseRequest(List.of(UUID.randomUUID()));

    mockMvc
        .perform(
            post("/api/v1/ia/groupage/propositions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isNotFound());
  }
}
