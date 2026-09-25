package com.logiflow.tms.ai.application.outils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.logiflow.tms.ai.domain.model.copilote.SourceCopilote;
import com.logiflow.tms.fleet.api.RemorqueApi;
import com.logiflow.tms.fleet.api.VehiculeApi;
import com.logiflow.tms.fleet.api.dto.RemorqueSummary;
import com.logiflow.tms.maintenance.api.MaintenanceApi;
import com.logiflow.tms.maintenance.api.dto.CoutsMaintenanceSummary;
import com.logiflow.tms.maintenance.api.dto.SinistreSummary;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OutilsMaintenanceCopiloteTest {

  @Mock private VehiculeApi vehiculeApi;
  @Mock private RemorqueApi remorqueApi;
  @Mock private MaintenanceApi maintenanceApi;

  private ResolveurEngin resolveur;

  @BeforeEach
  void preparer() {
    resolveur = new ResolveurEngin(new ResolveurVehicule(vehiculeApi), vehiculeApi, remorqueApi);
    lenient()
        .when(vehiculeApi.rechercher(any(), isNull(), any(PageRequest.class)))
        .thenReturn(Page.of(List.of(), 0, 10, 0));
    lenient()
        .when(remorqueApi.rechercher(any(), isNull(), any(PageRequest.class)))
        .thenReturn(Page.of(List.of(), 0, 10, 0));
    lenient().when(vehiculeApi.consulter(any())).thenReturn(Optional.empty());
  }

  private static SinistreSummary sinistre(UUID remorqueId, String statut, int joursAvant) {
    return new SinistreSummary(
        UUID.randomUUID(),
        "SIN-2026-00000" + joursAvant,
        null,
        remorqueId,
        null,
        LocalDateTime.now().minusDays(joursAvant),
        "ACCROCHAGE",
        "MATERIEL_LEGER",
        "PARTAGEE",
        statut,
        false,
        new BigDecimal("850.00"));
  }

  @Test
  void leResolveurRetrouveUneRemorqueQuandAucunVehiculeNeCorrespond() {
    var remorque = new RemorqueSummary(UUID.randomUUID(), "FR-456-GH", 90, 33, 25000, "DISPONIBLE");
    when(remorqueApi.rechercher(eq("FR-456-GH"), isNull(), any(PageRequest.class)))
        .thenReturn(Page.of(List.of(remorque), 0, 10, 1));

    var engin = resolveur.parImmatriculation("fr456gh");

    assertThat(engin.type()).isEqualTo("REMORQUE");
    assertThat(engin.id()).isEqualTo(remorque.id());
  }

  @Test
  void leResolveurSignaleUnEnginInconnu() {
    assertThatThrownBy(() -> resolveur.parImmatriculation("ZZ-999-ZZ"))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("ni aucune remorque");
  }

  @Test
  void rechercherSinistresNeGardeQueLesDossiersOuvertsDuPlusRecentAuPlusAncien() {
    UUID remorqueId = UUID.randomUUID();
    when(remorqueApi.consulter(remorqueId))
        .thenReturn(
            Optional.of(new RemorqueSummary(remorqueId, "FR-456-GH", 90, 33, 25000, "DISPONIBLE")));
    var ancien = sinistre(remorqueId, "EN_EXPERTISE", 40);
    var recent = sinistre(remorqueId, "DECLARE", 3);
    when(maintenanceApi.sinistres(isNull(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(List.of(ancien, sinistre(remorqueId, "CLOS", 10), recent));

    var resultat =
        new RechercherSinistresOutil(maintenanceApi, resolveur)
            .executer(new ArgumentsOutil(Map.of("statut", "ouverts")));

    assertThat(resultat.total()).isEqualTo(2);
    assertThat(resultat.resultats())
        .extracting(l -> l.get("reference"))
        .containsExactly(recent.reference(), ancien.reference());
    assertThat(resultat.resultats().getFirst())
        .containsEntry("engins", List.of("FR-456-GH"))
        .containsEntry("coutNet", new BigDecimal("850.00"))
        .containsEntry("devise", "MAD");
    assertThat(resultat.sources())
        .contains(new SourceCopilote("SINISTRE", recent.reference(), recent.id().toString()));
  }

  @Test
  void coutsMaintenanceResumeLaPeriodeEtLesPostes() {
    when(maintenanceApi.couts(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-06-30"), "REMORQUE"))
        .thenReturn(
            new CoutsMaintenanceSummary(
                LocalDate.parse("2026-01-01"),
                LocalDate.parse("2026-06-30"),
                new BigDecimal("4200.00"),
                new BigDecimal("5040.00"),
                6,
                new BigDecimal("4000.00"),
                List.of(
                    new CoutsMaintenanceSummary.Poste(
                        "GROUPE_FROID", "GROUPE_FROID", new BigDecimal("3000.00"), 2)),
                List.of(),
                List.of(),
                1,
                new BigDecimal("500.00"),
                new BigDecimal("1200.00")));

    var resultat =
        new CoutsMaintenanceOutil(maintenanceApi)
            .executer(
                new ArgumentsOutil(
                    Map.of(
                        "depuis", "2026-01-01", "jusqua", "2026-06-30", "typeEngin", "remorque")));

    assertThat(resultat.resultats().getFirst())
        .containsEntry("totalHt", new BigDecimal("4200.00"))
        .containsEntry("ecartBudget", new BigDecimal("200.00"))
        .containsEntry("coutNetSinistres", new BigDecimal("500.00"));
    assertThat(resultat.resultats().get(1))
        .containsEntry("postes", List.of("GROUPE_FROID : 3000.00 MAD HT (2 OT)"));
    assertThat(resultat.sources())
        .extracting(SourceCopilote::type)
        .containsExactly("COUTS_MAINTENANCE");
  }

  @Test
  void coutsMaintenanceRefuseUnePeriodeInversee() {
    var outil = new CoutsMaintenanceOutil(maintenanceApi);

    assertThatThrownBy(
            () ->
                outil.executer(
                    new ArgumentsOutil(Map.of("depuis", "2026-06-30", "jusqua", "2026-01-01"))))
        .isInstanceOf(ValidationException.class);
  }
}
