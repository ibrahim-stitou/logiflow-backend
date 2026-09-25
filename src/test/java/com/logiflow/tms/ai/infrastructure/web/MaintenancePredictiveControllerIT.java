package com.logiflow.tms.ai.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.logiflow.tms.ai.domain.model.maintenance.ContexteMaintenance;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;
import com.logiflow.tms.ai.domain.port.out.MaintenancePredictiveClientPort;
import com.logiflow.tms.fleet.domain.model.TypeVehicule;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.shared.AbstractIntegrationTest;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Test d'intégration du {@link MaintenancePredictiveController}. L'agent Flask est simulé : on
 * vérifie la collecte du contexte (plans, ordres de travail) et l'enregistrement du score de santé
 * dans le module maintenance.
 */
@AutoConfigureMockMvc
class MaintenancePredictiveControllerIT extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private MaintenancePredictiveClientPort clientPort;

  private UUID creerId(Object corps, String uri) throws Exception {
    String reponse =
        mockMvc
            .perform(
                post(uri)
                    .with(jwt())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(corps)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return UUID.fromString(objectMapper.readTree(reponse).get("id").asText());
  }

  private UUID creerVehicule() throws Exception {
    return creerId(
        new VehiculeRequest(
            "MP-" + (100 + (int) (Math.random() * 900)) + "-IT",
            TypeVehicule.TRACTEUR,
            null,
            null,
            null,
            null,
            null,
            null,
            40000,
            null,
            25000,
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
            null),
        "/api/v1/vehicules");
  }

  private static ResultatMaintenance analyse(ContexteMaintenance contexte, String vehiculeId) {
    var vehicule =
        contexte.vehicules().stream()
            .filter(v -> v.id().equals(vehiculeId))
            .findFirst()
            .orElseThrow();
    var recommandation =
        new ResultatMaintenance.Recommandation(
            "ENTRETIEN_PREVENTIF",
            "Révision à réaliser",
            "HAUTE",
            LocalDate.now().plusDays(5),
            null,
            null,
            120,
            "1 500 km restants",
            false);
    return new ResultatMaintenance(
        List.of(
            new ResultatMaintenance.AnalyseVehicule(
                vehicule.id(),
                vehicule.typeEngin(),
                vehicule.immatriculation(),
                52,
                "A_PLANIFIER",
                300,
                null,
                1500,
                LocalDate.now().plusDays(5),
                List.of(),
                List.of(),
                List.of(recommandation),
                "Révision imminente.")),
        "Une révision à planifier.",
        "GABARIT");
  }

  @Test
  void analyseUnVehiculeEtEnregistreSonScoreDeSante() throws Exception {
    UUID vehiculeId = creerVehicule();
    UUID planId =
        creerId(
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
                        "dureeEstimeeMin",
                        150)),
            "/api/v1/maintenance/plans");
    creerId(
        Map.of(
            "engin",
            Map.of("type", "VEHICULE", "id", vehiculeId),
            "origine",
            "PLAN_ENTRETIEN",
            "planId",
            planId,
            "details",
            Map.of(
                "type", "ENTRETIEN_PREVENTIF",
                "nature", "PREVENTIF",
                "titre", "Révision",
                "debutPlanifie", LocalDateTime.now().minusDays(100).withNano(0).toString())),
        "/api/v1/maintenance/ordres-travail");
    creerId(
        Map.of(
            "vehiculeId", vehiculeId,
            "dateSurvenance", LocalDateTime.now().minusDays(20).withNano(0).toString(),
            "type", "ACCROCHAGE",
            "gravite", "MATERIEL_LEGER",
            "description", "Rétroviseur arraché sur un quai."),
        "/api/v1/maintenance/sinistres");
    AtomicReference<ContexteMaintenance> recu = new AtomicReference<>();
    when(clientPort.recommander(any()))
        .thenAnswer(
            invocation -> {
              ContexteMaintenance contexte = invocation.getArgument(0);
              recu.set(contexte);
              return analyse(contexte, vehiculeId.toString());
            });

    mockMvc
        .perform(
            post("/api/v1/ia/maintenance/analyse")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("vehiculeId", vehiculeId))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vehicules[0].statut").value("A_PLANIFIER"))
        .andExpect(jsonPath("$.vehicules[0].typeEngin").value("VEHICULE"))
        .andExpect(jsonPath("$.vehicules[0].recommandations[0].priorite").value("HAUTE"))
        .andExpect(jsonPath("$.synthese").value("Une révision à planifier."));

    ContexteMaintenance.Vehicule envoye = recu.get().vehicules().getFirst();
    assertThat(recu.get().vehicules()).hasSize(1);
    assertThat(envoye.plans())
        .singleElement()
        .satisfies(
            p -> {
              assertThat(p.periodiciteKm()).isEqualTo(40000);
              assertThat(p.dureeEstimeeMin()).isEqualTo(150);
              assertThat(p.type()).isEqualTo("ENTRETIEN_PREVENTIF");
              assertThat(p.etat()).isNotNull();
            });
    assertThat(envoye.typeEngin()).isEqualTo("VEHICULE");
    assertThat(envoye.ordres())
        .singleElement()
        .satisfies(
            o -> {
              assertThat(o.type()).isEqualTo("ENTRETIEN_PREVENTIF");
              assertThat(o.origine()).isEqualTo("PLAN_ENTRETIEN");
              assertThat(o.planId()).isEqualTo(planId.toString());
            });
    assertThat(envoye.sinistres())
        .singleElement()
        .satisfies(
            sinistre -> {
              assertThat(sinistre.type()).isEqualTo("ACCROCHAGE");
              assertThat(sinistre.statut()).isEqualTo("DECLARE");
            });

    mockMvc
        .perform(
            get("/api/v1/scores-sante/dernier")
                .param("vehiculeId", vehiculeId.toString())
                .with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(52.0))
        .andExpect(jsonPath("$.recommandation").value("Révision imminente."));
  }

  @Test
  void agentIndisponibleRenvoie503() throws Exception {
    UUID vehiculeId = creerVehicule();
    when(clientPort.recommander(any()))
        .thenThrow(new ServiceIndisponibleException("Service IA indisponible"));

    mockMvc
        .perform(
            post("/api/v1/ia/maintenance/analyse")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("vehiculeId", vehiculeId))))
        .andExpect(status().isServiceUnavailable());
  }

  @Test
  void vehiculeInconnuRenvoie404() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ia/maintenance/analyse")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("vehiculeId", UUID.randomUUID()))))
        .andExpect(status().isNotFound());
  }
}
