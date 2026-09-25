package com.logiflow.tms.planning.infrastructure.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
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
import com.logiflow.tms.order.domain.vo.LigneCommande;
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
import com.logiflow.tms.referential.infrastructure.web.dto.MarchandiseRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.SiteRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Test d'intégration bout en bout du {@link VoyageController} : chaîne complète client -> commande
 * -> dossier -> voyage, arrêts construits depuis les sites des dossiers et contrôles de période.
 */
@AutoConfigureMockMvc
class VoyageControllerIT extends AbstractIntegrationTest {

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

  private static String suffixe() {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private UUID creerSite(String libelle, double latitude, double longitude) throws Exception {
    return creerId(
        objectMapper.writeValueAsString(
            new SiteRequest(
                "SITE-" + suffixe(),
                libelle,
                null,
                new GeoPoint(latitude, longitude),
                null,
                null,
                null)),
        "/api/v1/sites");
  }

  private UUID creerDossier(UUID siteChargement, UUID siteDechargement, Instant debut)
      throws Exception {
    String s = suffixe();
    UUID clientId =
        creerId(
            objectMapper.writeValueAsString(new ClientRequest("CLI-" + s, "Client voyage")),
            "/api/v1/clients");
    UUID marchandiseId =
        creerId(
            objectMapper.writeValueAsString(
                new MarchandiseRequest("MARCH-" + s, "Marchandise voyage", null, null, null, true)),
            "/api/v1/marchandises");
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
        .perform(put("/api/v1/commandes/{id}/confirmer", commandeId).with(jwt()))
        .andExpect(status().isOk());
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
                        siteChargement,
                        new TimeWindow(debut, debut.plus(2, ChronoUnit.HOURS)),
                        null),
                    new Segment(
                        TypeSegment.DECHARGEMENT,
                        1,
                        siteDechargement,
                        new TimeWindow(
                            debut.plus(5, ChronoUnit.HOURS), debut.plus(9, ChronoUnit.HOURS)),
                        null)),
                List.of())),
        "/api/v1/dossiers");
  }

  private UUID creerPorteur() throws Exception {
    String immatriculation = "VO-" + (100 + Math.abs(suffixe().hashCode() % 900)) + "-IT";
    return creerId(
        objectMapper.writeValueAsString(
            new VehiculeRequest(
                immatriculation,
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

  private UUID creerChauffeur() throws Exception {
    return creerId(
        objectMapper.writeValueAsString(
            new ChauffeurRequest("CH-" + suffixe(), "Voyage", "Jean", null, List.of(), 2100)),
        "/api/v1/chauffeurs");
  }

  private static VoyageRequest voyage(
      Instant depart, UUID vehiculeId, UUID dossierId, UUID chauffeurId) {
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
    return new VoyageRequest(
        TypeVoyage.SIMPLE,
        Portee.NATIONAL,
        depart,
        depart.plus(8, ChronoUnit.HOURS),
        vehiculeId,
        null,
        List.of(dossierId),
        trajet,
        List.of(new Affectation(chauffeurId, RoleChauffeur.TITULAIRE, depart)));
  }

  @Test
  void creerUnVoyageConformePersisteLesArretsEtPlanifieLeDossier() throws Exception {
    UUID lyon = creerSite("Lyon", 45.76, 4.84);
    UUID marseille = creerSite("Marseille", 43.30, 5.37);
    Instant depart = Instant.now().plus(2, ChronoUnit.DAYS);
    UUID dossierId = creerDossier(lyon, marseille, depart);
    UUID vehiculeId = creerPorteur();
    UUID chauffeurId = creerChauffeur();

    String reponse =
        mockMvc
            .perform(
                post("/api/v1/voyages")
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            voyage(depart, vehiculeId, dossierId, chauffeurId))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.statut").value("BROUILLON"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String voyageId = objectMapper.readTree(reponse).get("id").asText();

    mockMvc
        .perform(get("/api/v1/voyages/{id}/arrets", voyageId).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].siteId").value(lyon.toString()))
        .andExpect(jsonPath("$[1].siteId").value(marseille.toString()))
        .andExpect(jsonPath("$[1].latitude").value(43.30));

    mockMvc
        .perform(get("/api/v1/voyages/{id}/capacite", voyageId).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.troncons[0].poidsUtiliseKg").value(500.0));

    mockMvc
        .perform(get("/api/v1/dossiers/{id}", dossierId).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("PLANIFIE"));

    mockMvc
        .perform(get("/api/v1/voyages").param("chauffeurId", chauffeurId.toString()).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1));
  }

  @Test
  void unVehiculeDejaEngageSurLaPeriodeEstRefuseEtNestPlusProposeLibre() throws Exception {
    UUID lyon = creerSite("Lyon", 45.76, 4.84);
    UUID marseille = creerSite("Marseille", 43.30, 5.37);
    Instant depart = Instant.now().plus(10, ChronoUnit.DAYS);
    UUID vehiculeId = creerPorteur();
    UUID premierDossier = creerDossier(lyon, marseille, depart);
    UUID secondDossier = creerDossier(lyon, marseille, depart);
    UUID chauffeurA = creerChauffeur();
    UUID chauffeurB = creerChauffeur();
    String debut = depart.toString();
    String fin = depart.plus(8, ChronoUnit.HOURS).toString();

    mockMvc
        .perform(
            get("/api/v1/voyages/ressources-disponibles")
                .param("debut", debut)
                .param("fin", fin)
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vehicules[*].id", hasItem(vehiculeId.toString())));

    creerId(
        objectMapper.writeValueAsString(voyage(depart, vehiculeId, premierDossier, chauffeurA)),
        "/api/v1/voyages");

    mockMvc
        .perform(
            post("/api/v1/voyages")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        voyage(
                            depart.plus(4, ChronoUnit.HOURS),
                            vehiculeId,
                            secondDossier,
                            chauffeurB))))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.violations[0]").value(org.hamcrest.Matchers.containsString("déjà engagé")));

    mockMvc
        .perform(
            get("/api/v1/voyages/ressources-disponibles")
                .param("debut", debut)
                .param("fin", fin)
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vehicules[*].id", not(hasItem(vehiculeId.toString()))))
        .andExpect(jsonPath("$.chauffeurs[*].id", not(hasItem(chauffeurA.toString()))))
        .andExpect(jsonPath("$.chauffeurs[*].id", hasItem(chauffeurB.toString())));
  }

  @Test
  void conformiteABlancSignaleLesManquesSansEcrire() throws Exception {
    UUID lyon = creerSite("Lyon", 45.76, 4.84);
    UUID marseille = creerSite("Marseille", 43.30, 5.37);
    Instant depart = Instant.now().plus(20, ChronoUnit.DAYS);
    UUID dossierId = creerDossier(lyon, marseille, depart);

    mockMvc
        .perform(
            post("/api/v1/voyages/conformite")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "typeVoyage", "SIMPLE",
                            "departPrevu", depart.toString(),
                            "arriveePrevue", depart.plus(8, ChronoUnit.HOURS).toString(),
                            "dossierIds", List.of(dossierId)))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conforme").value(false))
        .andExpect(jsonPath("$.anomalies[*].code", hasItem("VEHICULE")))
        .andExpect(jsonPath("$.anomalies[*].code", hasItem("CHAUFFEUR")))
        .andExpect(jsonPath("$.arrets.length()").value(2))
        .andExpect(jsonPath("$.chargeMaxKg").value(500.0));

    mockMvc
        .perform(get("/api/v1/dossiers/{id}", dossierId).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("CREE"));
  }

  @Test
  void unVehiculeRetenuALAtelierSurLaPeriodeEstRefuse() throws Exception {
    UUID lyon = creerSite("Lyon", 45.76, 4.84);
    UUID marseille = creerSite("Marseille", 43.30, 5.37);
    Instant depart = Instant.now().plus(30, ChronoUnit.DAYS);
    UUID vehiculeId = creerPorteur();
    UUID dossierId = creerDossier(lyon, marseille, depart);
    UUID chauffeurId = creerChauffeur();
    java.time.LocalDateTime atelier =
        java.time.LocalDateTime.ofInstant(depart, java.time.ZoneId.of("Europe/Paris"))
            .minusHours(1);
    creerId(
        objectMapper.writeValueAsString(
            Map.of(
                "engin", Map.of("type", "VEHICULE", "id", vehiculeId),
                "details",
                    Map.of(
                        "type", "REPARATION",
                        "nature", "CORRECTIF",
                        "titre", "Remplacement embrayage",
                        "debutPlanifie", atelier.toString(),
                        "finPlanifiee", atelier.plusHours(6).toString()))),
        "/api/v1/maintenance/ordres-travail");

    mockMvc
        .perform(
            get("/api/v1/voyages/ressources-disponibles")
                .param("debut", depart.toString())
                .param("fin", depart.plus(8, ChronoUnit.HOURS).toString())
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vehicules[*].id", not(hasItem(vehiculeId.toString()))));

    mockMvc
        .perform(
            post("/api/v1/voyages")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        voyage(depart, vehiculeId, dossierId, chauffeurId))))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.violations[*]", hasItem(org.hamcrest.Matchers.containsString("atelier"))));
  }

  @Test
  void accederSansAuthentificationRenvoie401() throws Exception {
    mockMvc.perform(get("/api/v1/voyages")).andExpect(status().isUnauthorized());
  }
}
