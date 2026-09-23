package com.logiflow.tms.ai.application.outils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.carburant.api.CarburantApi;
import com.logiflow.tms.carburant.api.dto.ConsommationCarburantSummary;
import com.logiflow.tms.dossier.api.DossierApi;
import com.logiflow.tms.dossier.api.dto.DossierSummary;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.VehiculeSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutilsCopiloteTest {

  private static final Clock HORLOGE =
      Clock.fixed(Instant.parse("2026-09-23T10:00:00Z"), ZoneId.of("Europe/Paris"));

  @Mock private DossierApi dossierApi;
  @Mock private VehiculeApi vehiculeApi;
  @Mock private CarburantApi carburantApi;

  private static VehiculeSummary vehicule(String immatriculation) {
    return new VehiculeSummary(
        UUID.randomUUID(), immatriculation, "TRACTEUR", 44000, 25000, "DISPONIBLE");
  }

  @Test
  void rechercherDossiersRenvoieLignesTotalEtSources() {
    var id = UUID.randomUUID();
    when(dossierApi.statutsConnus()).thenReturn(List.of("CREE", "EN_TRANSIT"));
    when(dossierApi.rechercher(eq(null), eq("EN_TRANSIT"), any(PageRequest.class)))
        .thenReturn(
            Page.of(
                List.of(
                    new DossierSummary(
                        id, "DOS-2026-00012", "EN_TRANSIT", true, 1200, 8, 4, false)),
                0,
                10,
                37));

    var resultat =
        new RechercherDossiersOutil(dossierApi)
            .executer(new ArgumentsOutil(Map.of("statut", "en_transit")));

    assertThat(resultat.total()).isEqualTo(37);
    assertThat(resultat.resultats().getFirst())
        .containsEntry("reference", "DOS-2026-00012")
        .containsEntry("poidsBrutKg", 1200.0)
        .containsEntry("adr", false);
    assertThat(resultat.sources())
        .containsExactly(new SourceCopilote("DOSSIER", "DOS-2026-00012", id.toString()));
  }

  @Test
  void leResolveurRetrouveUneImmatriculationSaisieSansTirets() {
    var attendu = vehicule("AB-123-CD");
    when(vehiculeApi.rechercher(eq("ab123cd"), isNull(), any(PageRequest.class)))
        .thenReturn(Page.of(List.of(), 0, 10, 0));
    when(vehiculeApi.rechercher(eq("AB123CD"), isNull(), any(PageRequest.class)))
        .thenReturn(Page.of(List.of(), 0, 10, 0));
    when(vehiculeApi.rechercher(eq("AB-123-CD"), isNull(), any(PageRequest.class)))
        .thenReturn(Page.of(List.of(vehicule("AB-123-CE"), attendu), 0, 10, 2));

    assertThat(new ResolveurVehicule(vehiculeApi).parImmatriculation("ab123cd")).isEqualTo(attendu);
  }

  @Test
  void leResolveurSignaleUneImmatriculationInconnue() {
    when(vehiculeApi.rechercher(any(), isNull(), any(PageRequest.class)))
        .thenReturn(Page.of(List.of(), 0, 10, 0));

    assertThatThrownBy(() -> new ResolveurVehicule(vehiculeApi).parImmatriculation("ZZ-999-ZZ"))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("ZZ-999-ZZ");
  }

  @Test
  void consommationCarburantParDefautDuPremierDuMoisAAujourdhuiPourLaFlotte() {
    var debut = LocalDate.of(2026, 9, 1);
    var fin = LocalDate.of(2026, 9, 23);
    when(carburantApi.consommation(null, debut, fin))
        .thenReturn(
            new ConsommationCarburantSummary(
                null,
                debut,
                fin,
                12,
                3400.5,
                new BigDecimal("6120.90"),
                List.of(
                    new ConsommationCarburantSummary.ParType(
                        "GAZOLE", 12, 3400.5, new BigDecimal("6120.90")))));

    var resultat =
        new ConsommationCarburantOutil(carburantApi, new ResolveurVehicule(vehiculeApi), HORLOGE)
            .executer(new ArgumentsOutil(Map.of()));

    verify(carburantApi).consommation(null, debut, fin);
    assertThat(resultat.resultats().getFirst())
        .containsEntry("vehicule", "toute la flotte")
        .containsEntry("litres", 3400.5)
        .containsEntry("nombrePrises", 12L);
    assertThat(resultat.sources()).isEmpty();
  }

  @Test
  void consommationCarburantRefuseUnePeriodeInversee() {
    var outil =
        new ConsommationCarburantOutil(carburantApi, new ResolveurVehicule(vehiculeApi), HORLOGE);

    assertThatThrownBy(
            () ->
                outil.executer(
                    new ArgumentsOutil(Map.of("debut", "2026-09-10", "fin", "2026-09-01"))))
        .isInstanceOf(ValidationException.class);
  }
}
