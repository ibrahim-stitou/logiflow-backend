package com.logiflow.tms.planning.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.domain.model.TypeSegment;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import com.logiflow.tms.dossier.infrastructure.web.dto.DossierRequest;
import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurRequest;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.infrastructure.web.dto.RemorqueRequest;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.order.domain.vo.LigneCommande;
import com.logiflow.tms.order.infrastructure.web.dto.CommandeRequest;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.TypeEtape;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Etape;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.planning.infrastructure.web.dto.AjouterDossierVoyageRequest;
import com.logiflow.tms.planning.infrastructure.web.dto.VoyageRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.ClientRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.MarchandiseRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.TimeWindow;
import jakarta.persistence.EntityManager;
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

/** Tests d'intégration de l'ajout transactionnel d'un dossier à un voyage. */
@AutoConfigureMockMvc
class VoyageDossierControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private VoyageArretRepository voyageArretRepository;
  @Autowired private DossierApi dossierApi;
  @Autowired private EntityManager entityManager;

  private record ContexteVoyage(
      UUID voyageId, UUID arretA, UUID arretB, UUID arretC, UUID dossierInitialId) {}

  @Test
  void ajouterUnDossierCompatibleReussit() throws Exception {
    ContexteVoyage contexte = preparerVoyageAvecArrets(500, 2.0);
    UUID dossier2 = creerDossier(300, 1.5);

    var requete =
        new AjouterDossierVoyageRequest(
            dossier2,
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretB(), null),
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretC(), null),
            null);

    long arretsAvant = voyageArretRepository.compterParVoyageId(contexte.voyageId());

    mockMvc
        .perform(
            post("/api/v1/voyages/{id}/dossiers", contexte.voyageId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated());

    assertThat(voyageArretRepository.compterParVoyageId(contexte.voyageId()))
        .isEqualTo(arretsAvant);
    mockMvc
        .perform(getDossier(dossier2))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("PLANIFIE"));
  }

  @Test
  void verifierAjoutDossierCompatible() throws Exception {
    ContexteVoyage contexte = preparerVoyageAvecArrets(500, 2.0);
    UUID dossier2 = creerDossier(300, 1.5);

    var requete =
        new AjouterDossierVoyageRequest(
            dossier2,
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretB(), null),
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretC(), null),
            null);

    mockMvc
        .perform(
            post("/api/v1/voyages/{id}/dossiers/check", contexte.voyageId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.compatible").value(true))
        .andExpect(jsonPath("$.failedLegs").isEmpty());
  }

  @Test
  void verifierAjoutDossierSignaleCapaciteInsuffisante() throws Exception {
    ContexteVoyage contexte = preparerVoyageAvecArrets(23000, 5.0);
    UUID dossier2 = creerDossier(2000, 1.0);

    var requete =
        new AjouterDossierVoyageRequest(
            dossier2,
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretB(), null),
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretC(), null),
            null);

    mockMvc
        .perform(
            post("/api/v1/voyages/{id}/dossiers/check", contexte.voyageId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.compatible").value(false))
        .andExpect(jsonPath("$.failedLegs").isNotEmpty());
  }

  @Test
  void deviationExcessiveNeCreePasDArretOrphelin() throws Exception {
    ContexteVoyage contexte = preparerVoyageAvecArrets(500, 2.0);
    UUID dossier2 = creerDossier(300, 1.5);
    long arretsAvant = voyageArretRepository.compterParVoyageId(contexte.voyageId());

    var requete =
        new AjouterDossierVoyageRequest(
            dossier2,
            new AjouterDossierVoyageRequest.SelectionArretRequest(
                null, new AjouterDossierVoyageRequest.NouvelArretRequest("Hors route", 0.5, 0.5)),
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretC(), null),
            1d);

    mockMvc
        .perform(
            post("/api/v1/voyages/{id}/dossiers", contexte.voyageId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value("ROUTE_DEVIATION_EXCEEDED"));

    assertThat(voyageArretRepository.compterParVoyageId(contexte.voyageId()))
        .isEqualTo(arretsAvant);
    mockMvc.perform(getDossier(dossier2)).andExpect(jsonPath("$.statut").value("CREE"));
  }

  @Test
  void memeArretChargementEtDechargementEstRefuse() throws Exception {
    ContexteVoyage contexte = preparerVoyageAvecArrets(500, 2.0);
    UUID dossier2 = creerDossier(300, 1.5);

    var requete =
        new AjouterDossierVoyageRequest(
            dossier2,
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretB(), null),
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretB(), null),
            null);

    mockMvc
        .perform(
            post("/api/v1/voyages/{id}/dossiers/check", contexte.voyageId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void ajoutSurRouteDeDeuxArretsReussit() throws Exception {
    ContexteVoyage contexte = preparerVoyageDeuxArrets(800, 3.0);
    UUID dossier2 = creerDossier(200, 1.0);

    var requete =
        new AjouterDossierVoyageRequest(
            dossier2,
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretA(), null),
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretB(), null),
            null);

    mockMvc
        .perform(
            post("/api/v1/voyages/{id}/dossiers", contexte.voyageId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated());
  }

  @Test
  void annulationVoyagePurgeArretInsereEtReinitialiseDossiers() throws Exception {
    ContexteVoyage contexte = preparerVoyageAvecArrets(500, 2.0);
    UUID dossier2 = creerDossier(300, 1.5);
    long arretsAvant = voyageArretRepository.compterParVoyageId(contexte.voyageId());

    var requete =
        new AjouterDossierVoyageRequest(
            dossier2,
            new AjouterDossierVoyageRequest.SelectionArretRequest(
                null, new AjouterDossierVoyageRequest.NouvelArretRequest("Relais", 0, 0.5)),
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretC(), null),
            null);

    mockMvc
        .perform(
            post("/api/v1/voyages/{id}/dossiers", contexte.voyageId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isCreated());

    assertThat(voyageArretRepository.compterParVoyageId(contexte.voyageId()))
        .isEqualTo(arretsAvant + 1);

    mockMvc
        .perform(
            put("/api/v1/voyages/{id}/statut", contexte.voyageId())
                .with(jwt())
                .param("valeur", "ANNULE"))
        .andExpect(status().isOk());

    assertThat(voyageArretRepository.compterParVoyageId(contexte.voyageId()))
        .isEqualTo(arretsAvant);
    mockMvc.perform(getDossier(dossier2)).andExpect(jsonPath("$.statut").value("CREE"));
  }

  @Test
  void capaciteInsuffisanteNePersistePasLeDossier() throws Exception {
    ContexteVoyage contexte = preparerVoyageAvecArrets(23000, 5.0);
    UUID dossier2 = creerDossier(2000, 1.0);
    long arretsAvant = voyageArretRepository.compterParVoyageId(contexte.voyageId());

    var requete =
        new AjouterDossierVoyageRequest(
            dossier2,
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretB(), null),
            new AjouterDossierVoyageRequest.SelectionArretRequest(contexte.arretC(), null),
            null);

    mockMvc
        .perform(
            post("/api/v1/voyages/{id}/dossiers", contexte.voyageId())
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requete)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value("REMORQUE_CAPACITY_EXCEEDED"));

    assertThat(voyageArretRepository.compterParVoyageId(contexte.voyageId()))
        .isEqualTo(arretsAvant);
    mockMvc.perform(getDossier(dossier2)).andExpect(jsonPath("$.statut").value("CREE"));
  }

  private org.springframework.test.web.servlet.RequestBuilder getDossier(UUID dossierId) {
    return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
            "/api/v1/dossiers/{id}", dossierId)
        .with(jwt());
  }

  private ContexteVoyage preparerVoyageDeuxArrets(double poidsInitialKg, double volumeInitialM3)
      throws Exception {
    ContexteVoyage contexte = preparerVoyageSansArrets(poidsInitialKg, volumeInitialM3);
    sauvegarderArret(contexte.arretA(), contexte.voyageId(), 0, "Point A", 0, 0);
    sauvegarderArret(contexte.arretB(), contexte.voyageId(), 1, "Point B", 0, 2);
    rafraichirContextePersistence();
    dossierApi.affecterArretsVoyage(
        contexte.dossierInitialId(), contexte.arretA(), contexte.arretB());
    rafraichirContextePersistence();
    return new ContexteVoyage(
        contexte.voyageId(),
        contexte.arretA(),
        contexte.arretB(),
        contexte.arretB(),
        contexte.dossierInitialId());
  }

  private ContexteVoyage preparerVoyageSansArrets(double poidsInitialKg, double volumeInitialM3)
      throws Exception {
    String suffixe = UUID.randomUUID().toString().substring(0, 8);
    int numero = 100 + Math.abs(suffixe.hashCode() % 900);
    String immatVehicule = "VD-" + numero + "-AB";
    String immatRemorque = "RM-" + numero + "-AB";
    UUID clientId =
        creerId(
            objectMapper.writeValueAsString(
                new ClientRequest("CLI-VD-" + suffixe, "Client voyage dossier")),
            "/api/v1/clients");
    UUID marchandiseId =
        creerId(
            objectMapper.writeValueAsString(
                new MarchandiseRequest(
                    "MARCH-VD-" + suffixe, "Marchandise", null, null, null, true)),
            "/api/v1/marchandises");
    UUID commandeId = creerCommandeConfirmee(clientId, marchandiseId);

    UUID dossierInitialId =
        creerDossierAvecPoids(commandeId, marchandiseId, poidsInitialKg, volumeInitialM3);

    UUID vehiculeId =
        creerId(
            objectMapper.writeValueAsString(
                new VehiculeRequest(
                    immatVehicule,
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
    UUID remorqueId =
        creerId(
            objectMapper.writeValueAsString(
                new RemorqueRequest(
                    immatRemorque,
                    null,
                    "TAUTLINER",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    80,
                    33,
                    24000,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null)),
            "/api/v1/remorques");
    UUID chauffeurId =
        creerId(
            objectMapper.writeValueAsString(
                new ChauffeurRequest("CH-VD-" + suffixe, "Dupont", "Jean", null, List.of(), 2100)),
            "/api/v1/chauffeurs");

    Instant depart = Instant.now();
    Trajet trajet =
        new Trajet(
            450,
            360,
            420,
            List.of(
                new Etape(
                    0, TypeEtape.CHARGEMENT, depart, depart.plus(1, ChronoUnit.HOURS), 0, 500),
                new Etape(
                    1, TypeEtape.DECHARGEMENT, depart.plus(7, ChronoUnit.HOURS), null, 450, 0)));

    UUID voyageId =
        creerId(
            objectMapper.writeValueAsString(
                new VoyageRequest(
                    TypeVoyage.SIMPLE,
                    Portee.NATIONAL,
                    depart,
                    depart.plus(8, ChronoUnit.HOURS),
                    vehiculeId,
                    remorqueId,
                    List.of(dossierInitialId),
                    trajet,
                    List.of(new Affectation(chauffeurId, RoleChauffeur.TITULAIRE, depart)))),
            "/api/v1/voyages");

    UUID arretA = UUID.randomUUID();
    UUID arretB = UUID.randomUUID();
    UUID arretC = UUID.randomUUID();
    return new ContexteVoyage(voyageId, arretA, arretB, arretC, dossierInitialId);
  }

  private ContexteVoyage preparerVoyageAvecArrets(double poidsInitialKg, double volumeInitialM3)
      throws Exception {
    ContexteVoyage base = preparerVoyageSansArrets(poidsInitialKg, volumeInitialM3);
    sauvegarderArret(base.arretA(), base.voyageId(), 0, "Point A", 0, 0);
    sauvegarderArret(base.arretB(), base.voyageId(), 1, "Point B", 0, 1);
    sauvegarderArret(base.arretC(), base.voyageId(), 2, "Point C", 0, 2);
    rafraichirContextePersistence();
    dossierApi.affecterArretsVoyage(base.dossierInitialId(), base.arretA(), base.arretC());
    rafraichirContextePersistence();
    return base;
  }

  private UUID creerDossier(double poidsKg, double volumeM3) throws Exception {
    UUID marchandiseId =
        creerId(
            objectMapper.writeValueAsString(
                new MarchandiseRequest(
                    "MARCH-" + UUID.randomUUID().toString().substring(0, 8),
                    "Marchandise",
                    null,
                    null,
                    null,
                    true)),
            "/api/v1/marchandises");
    UUID clientId =
        creerId(
            objectMapper.writeValueAsString(
                new ClientRequest("CLI-" + UUID.randomUUID().toString().substring(0, 8), "Client")),
            "/api/v1/clients");
    UUID commandeId = creerCommandeConfirmee(clientId, marchandiseId);
    return creerDossierAvecPoids(commandeId, marchandiseId, poidsKg, volumeM3);
  }

  private UUID creerDossierAvecPoids(
      UUID commandeId, UUID marchandiseId, double poidsKg, double volumeM3) throws Exception {
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
                List.of(
                    new LigneMarchandise(marchandiseId, poidsKg, volumeM3, 10, null, null, true)),
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

  private UUID creerCommandeConfirmee(UUID clientId, UUID marchandiseId) throws Exception {
    UUID commandeId =
        creerId(
            objectMapper.writeValueAsString(
                new CommandeRequest(
                    clientId,
                    LocalDate.now().plusDays(3),
                    new Money(BigDecimal.valueOf(2000), Currency.getInstance("EUR")),
                    List.of(new LigneCommande(marchandiseId, 500, 2.5, 10)))),
            "/api/v1/commandes");
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/v1/commandes/{id}/confirmer", commandeId)
                .with(jwt()))
        .andExpect(status().isOk());
    return commandeId;
  }

  private void sauvegarderArret(
      UUID id, UUID voyageId, int indice, String libelle, double lat, double lon) {
    voyageArretRepository.sauvegarder(
        ArretVoyage.creer(id, voyageId, indice, libelle, new GeoPoint(lat, lon), null, true));
  }

  private void rafraichirContextePersistence() {
    entityManager.flush();
    entityManager.clear();
  }

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
}
