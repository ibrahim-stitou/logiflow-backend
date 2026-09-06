package com.logiflow.tms.tracking.infrastructure.web;

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
import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurRequest;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.order.infrastructure.web.dto.CommandeRequest;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.TypeEtape;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Etape;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.planning.infrastructure.web.dto.VoyageRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.ClientRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.TimeWindow;
import com.logiflow.tms.tracking.domain.model.TypeEvenement;
import com.logiflow.tms.tracking.infrastructure.web.dto.EvenementVoyageRequest;
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
 * Test d'intégration bout en bout du {@link EvenementVoyageController} : chaîne complète jusqu'au
 * voyage.
 */
@AutoConfigureMockMvc
class EvenementVoyageControllerIT extends AbstractIntegrationTest {

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

  private UUID creerVoyage() throws Exception {
    UUID clientId =
        creerId(
            objectMapper.writeValueAsString(new ClientRequest("CLI-IT-TRACK", "Client tracking")),
            "/api/v1/clients");
    UUID commandeId =
        creerId(
            objectMapper.writeValueAsString(
                new CommandeRequest(
                    clientId,
                    LocalDate.now().plusDays(3),
                    new Money(BigDecimal.valueOf(1800), Currency.getInstance("EUR")))),
            "/api/v1/commandes");
    mockMvc
        .perform(put("/api/v1/commandes/{id}/confirmer", commandeId).with(jwt()))
        .andExpect(status().isOk());

    Instant maintenant = Instant.now();
    UUID dossierId =
        creerId(
            objectMapper.writeValueAsString(
                new DossierRequest(
                    commandeId,
                    TypeTransport.NATIONAL,
                    true,
                    10,
                    "Palettes",
                    null,
                    null,
                    List.of(new LigneMarchandise("Palette", 500, 2.5, 10, null, null, true)),
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

    UUID vehiculeId =
        creerId(
            objectMapper.writeValueAsString(
                new VehiculeRequest(
                    "TR-IT-001", TypeVehicule.PORTEUR, null, null, null, null, null, null, 19000,
                    null, 9000, null, null, null, null, null, null, false, null, null, null, null,
                    null)),
            "/api/v1/vehicules");
    UUID chauffeurId =
        creerId(
            objectMapper.writeValueAsString(
                new ChauffeurRequest(
                    "CH-IT-TRACK", "Tracking", "Jean", null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, List.of(), 2100)),
            "/api/v1/chauffeurs");

    Trajet trajet =
        new Trajet(
            450,
            360,
            420,
            List.of(
                new Etape(
                    0,
                    TypeEtape.CHARGEMENT,
                    maintenant,
                    maintenant.plus(1, ChronoUnit.HOURS),
                    0,
                    500),
                new Etape(
                    1,
                    TypeEtape.DECHARGEMENT,
                    maintenant.plus(7, ChronoUnit.HOURS),
                    null,
                    450,
                    0)));
    var voyageRequest =
        new VoyageRequest(
            TypeVoyage.SIMPLE,
            Portee.NATIONAL,
            maintenant,
            maintenant.plus(8, ChronoUnit.HOURS),
            vehiculeId,
            null,
            List.of(dossierId),
            trajet,
            List.of(new Affectation(chauffeurId, RoleChauffeur.TITULAIRE, maintenant)));

    return creerId(objectMapper.writeValueAsString(voyageRequest), "/api/v1/voyages");
  }

  @Test
  void declarerUnEvenementPourUnVoyageExistant() throws Exception {
    UUID voyageId = creerVoyage();
    var requete =
        new EvenementVoyageRequest(
            voyageId, TypeEvenement.DEPART, Instant.now(), new GeoPoint(48.8566, 2.3522), null);

    mockMvc
        .perform(
            post("/api/v1/evenements-voyage")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("DEPART"));

    mockMvc
        .perform(get("/api/v1/voyages/{voyageId}/evenements", voyageId).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].type").value("DEPART"));
  }

  @Test
  void declarerUnEvenementPourUnVoyageInexistantRenvoie404() throws Exception {
    var requete =
        new EvenementVoyageRequest(
            UUID.randomUUID(), TypeEvenement.DEPART, Instant.now(), null, null);

    mockMvc
        .perform(
            post("/api/v1/evenements-voyage")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isNotFound());
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc
        .perform(get("/api/v1/evenements-voyage/{id}", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }
}
