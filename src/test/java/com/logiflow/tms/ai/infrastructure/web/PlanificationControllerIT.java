package com.logiflow.tms.ai.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.ai.domain.model.planification.ContextePlanification;
import com.logiflow.tms.ai.domain.model.planification.ResultatPlanification;
import com.logiflow.tms.ai.domain.port.out.PlanificationClientPort;
import com.logiflow.tms.dossier.domain.model.TypeSegment;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import com.logiflow.tms.dossier.infrastructure.web.dto.DossierRequest;
import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurRequest;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.order.domain.vo.LigneCommande;
import com.logiflow.tms.order.infrastructure.web.dto.CommandeRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.ClientRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.MarchandiseRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.SiteRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.TimeWindow;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Test d'intégration du {@link PlanificationController}. L'agent Flask est simulé : il renvoie une
 * option bâtie sur le contexte reçu, ce qui vérifie la collecte du contexte (dossiers de la
 * période, ressources libres) et la revalidation réelle par le module planning.
 */
@AutoConfigureMockMvc
class PlanificationControllerIT extends AbstractIntegrationTest {

  private static final Instant DEBUT = Instant.now().plus(200, ChronoUnit.DAYS);

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private PlanificationClientPort planificationClientPort;

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

  private UUID creerSite(String code, double lat, double lon) throws Exception {
    return creerId(
        objectMapper.writeValueAsString(
            new SiteRequest(code, code, null, new GeoPoint(lat, lon), null, null, null)),
        "/api/v1/sites");
  }

  private UUID creerDossier(UUID de, UUID vers) throws Exception {
    String s = UUID.randomUUID().toString().substring(0, 8);
    UUID clientId =
        creerId(
            objectMapper.writeValueAsString(new ClientRequest("CLI-PL-" + s, "Client")),
            "/api/v1/clients");
    UUID marchandiseId =
        creerId(
            objectMapper.writeValueAsString(
                new MarchandiseRequest("MARCH-PL-" + s, "Marchandise", null, null, null, true)),
            "/api/v1/marchandises");
    UUID commandeId =
        creerId(
            objectMapper.writeValueAsString(
                new CommandeRequest(
                    clientId,
                    LocalDate.now().plusDays(3),
                    new Money(BigDecimal.valueOf(1000), Currency.getInstance("EUR")),
                    List.of(new LigneCommande(marchandiseId, 800, 3, 4)))),
            "/api/v1/commandes");
    mockMvc
        .perform(put("/api/v1/commandes/{id}/confirmer", commandeId).with(jwt()))
        .andExpect(status().isOk());
    return creerId(
        objectMapper.writeValueAsString(
            new DossierRequest(
                commandeId,
                TypeTransport.NATIONAL,
                true,
                4,
                "Palettes",
                null,
                null,
                List.of(new LigneMarchandise(marchandiseId, 800, 3, 4, null, null, true)),
                List.of(
                    new Segment(
                        TypeSegment.CHARGEMENT,
                        0,
                        de,
                        new TimeWindow(DEBUT, DEBUT.plus(3, ChronoUnit.HOURS)),
                        null),
                    new Segment(
                        TypeSegment.DECHARGEMENT,
                        1,
                        vers,
                        new TimeWindow(
                            DEBUT.plus(4, ChronoUnit.HOURS), DEBUT.plus(9, ChronoUnit.HOURS)),
                        null)),
                List.of())),
        "/api/v1/dossiers");
  }

  private UUID creerPorteur() throws Exception {
    return creerId(
        objectMapper.writeValueAsString(
            new VehiculeRequest(
                "PL-" + (100 + (int) (Math.random() * 900)) + "-IT",
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
                null)),
        "/api/v1/vehicules");
  }

  /**
   * Agent simulé : une option qui reprend dossiers, premier véhicule et premier chauffeur reçus.
   */
  private static ResultatPlanification optionDepuis(ContextePlanification c) {
    ContextePlanification.Dossier d = c.dossiers().getFirst();
    Instant depart = d.chargement().debut();
    Instant arrivee = d.dechargement().debut().plus(1, ChronoUnit.HOURS);
    var indicateurs =
        new ResultatPlanification.Indicateurs(
            1, 310, 240, 360, d.poidsBrutKg(), d.volumeM3(), 4, 0.09, 0.0, 0, 520, 650d);
    var arrets =
        List.of(
            new ResultatPlanification.Arret(
                0,
                d.chargement().siteId(),
                "A",
                45.76,
                4.84,
                List.of(d.id()),
                List.of(),
                depart,
                depart.plus(45, ChronoUnit.MINUTES),
                0,
                0,
                d.poidsBrutKg(),
                0,
                true),
            new ResultatPlanification.Arret(
                1,
                d.dechargement().siteId(),
                "B",
                43.30,
                5.37,
                List.of(),
                List.of(d.id()),
                d.dechargement().debut(),
                arrivee,
                310,
                240,
                0,
                0,
                true));
    return new ResultatPlanification(
        List.of(
            new ResultatPlanification.Option(
                1,
                "REMPLISSAGE",
                "Remplissage maximal",
                "SIMPLE",
                List.of(d.id()),
                arrets,
                c.vehicules().getFirst().id(),
                null,
                List.of(c.chauffeurs().getFirst().id()),
                depart,
                arrivee,
                indicateurs,
                List.of(),
                "Option simulée",
                true)),
        "Comparaison simulée",
        List.of(),
        "HAVERSINE",
        "GABARIT");
  }

  @Test
  void proposeDesVoyagesRevalidesAvecUnVoyagePretASoumettre() throws Exception {
    UUID lyon = creerSite("PL-LYON", 45.76, 4.84);
    UUID marseille = creerSite("PL-MRS", 43.30, 5.37);
    UUID dossierId = creerDossier(lyon, marseille);
    UUID vehiculeId = creerPorteur();
    UUID chauffeurId =
        creerId(
            objectMapper.writeValueAsString(
                new ChauffeurRequest("CH-PL-IT", "Planif", "Jean", null, List.of(), 2400)),
            "/api/v1/chauffeurs");
    AtomicReference<ContextePlanification> recu = new AtomicReference<>();
    when(planificationClientPort.proposer(any()))
        .thenAnswer(
            invocation -> {
              ContextePlanification contexte = invocation.getArgument(0);
              recu.set(contexte);
              return optionDepuis(contexte);
            });

    mockMvc
        .perform(
            post("/api/v1/ia/planification/propositions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "debut",
                            DEBUT.minus(1, ChronoUnit.HOURS).toString(),
                            "fin",
                            DEBUT.plus(1, ChronoUnit.DAYS).toString(),
                            "typeVoyage",
                            "GROUPAGE",
                            "nbOptions",
                            3))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nbDossiersCandidats").value(1))
        .andExpect(jsonPath("$.options[0].conformite.conforme").value(true))
        .andExpect(jsonPath("$.options[0].voyage.dossierIds[0]").value(dossierId.toString()))
        .andExpect(jsonPath("$.options[0].voyage.affectations[0].role").value("TITULAIRE"))
        .andExpect(jsonPath("$.options[0].voyage.arrets[0].siteId").value(lyon.toString()))
        .andExpect(jsonPath("$.options[0].voyage.trajet.etapes.length()").value(2))
        .andExpect(jsonPath("$.libelles.dossiers." + dossierId).exists());

    ContextePlanification contexte = recu.get();
    assertThat(contexte.dossiers())
        .extracting(ContextePlanification.Dossier::id)
        .containsExactly(dossierId.toString());
    assertThat(contexte.sites())
        .extracting(ContextePlanification.Site::id)
        .contains(lyon.toString(), marseille.toString());
    assertThat(contexte.vehicules())
        .extracting(ContextePlanification.Vehicule::id)
        .contains(vehiculeId.toString());
    assertThat(contexte.chauffeurs())
        .extracting(ContextePlanification.Chauffeur::id)
        .contains(chauffeurId.toString());
  }

  @Test
  void sansDossierALaPeriodeLAgentNestPasSollicite() throws Exception {
    Instant loin = Instant.now().plus(400, ChronoUnit.DAYS);
    mockMvc
        .perform(
            post("/api/v1/ia/planification/propositions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "debut", loin.toString(),
                            "fin", loin.plus(1, ChronoUnit.DAYS).toString()))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.options.length()").value(0))
        .andExpect(jsonPath("$.nbDossiersCandidats").value(0));
  }

  @Test
  void agentIndisponibleRenvoie503() throws Exception {
    UUID lyon = creerSite("PL-LYON-2", 45.76, 4.84);
    UUID marseille = creerSite("PL-MRS-2", 43.30, 5.37);
    creerDossier(lyon, marseille);
    when(planificationClientPort.proposer(any()))
        .thenThrow(new ServiceIndisponibleException("Service IA indisponible"));

    mockMvc
        .perform(
            post("/api/v1/ia/planification/propositions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "debut", DEBUT.minus(1, ChronoUnit.HOURS).toString(),
                            "fin", DEBUT.plus(1, ChronoUnit.DAYS).toString()))))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void periodeInverseeEstRefusee() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ia/planification/propositions")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "debut", DEBUT.toString(),
                            "fin", DEBUT.minus(1, ChronoUnit.DAYS).toString()))))
        .andExpect(status().is4xxClientError());
  }
}
