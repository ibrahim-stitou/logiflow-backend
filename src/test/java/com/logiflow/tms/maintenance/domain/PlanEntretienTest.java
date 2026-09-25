package com.logiflow.tms.maintenance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.maintenance.domain.model.EtatEcheance;
import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.domain.vo.Echeance;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlanEntretienTest {

  private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 1);

  private static PlanEntretien plan(
      Integer km, Integer mois, Integer heures, PlanEntretien.DerniereRealisation derniere) {
    return PlanEntretien.creer(
        UUID.randomUUID(),
        EnginRef.vehicule(UUID.randomUUID()),
        new PlanEntretien.Parametres(
            "Révision",
            TypeIntervention.ENTRETIEN_PREVENTIF,
            km,
            mois,
            heures,
            2000,
            15,
            120,
            null,
            null,
            true),
        derniere);
  }

  @Test
  void echeanceKilometriqueDepuisLaDerniereRealisationEtProjectionDeDate() {
    PlanEntretien p =
        plan(
            40_000,
            null,
            null,
            new PlanEntretien.DerniereRealisation(AUJOURDHUI.minusDays(100), 100_000, null));

    Echeance e = p.prochaineEcheance(130_000, 0, AUJOURDHUI, 300);

    assertThat(e.kmRestant()).isEqualTo(10_000);
    assertThat(e.dateEcheance()).isEqualTo(AUJOURDHUI.plusDays(34));
    assertThat(e.etat()).isEqualTo(EtatEcheance.OK);
  }

  @Test
  void alerteSousLeSeuilEtEchuAuDela() {
    PlanEntretien p =
        plan(
            40_000,
            null,
            null,
            new PlanEntretien.DerniereRealisation(AUJOURDHUI.minusDays(100), 100_000, null));

    assertThat(p.prochaineEcheance(138_500, 0, AUJOURDHUI, 300).etat())
        .isEqualTo(EtatEcheance.ALERTE);
    assertThat(p.prochaineEcheance(141_000, 0, AUJOURDHUI, 300).etat())
        .isEqualTo(EtatEcheance.ECHU);
  }

  @Test
  void echeanceCalendaireEtHeuresDeFonctionnement() {
    PlanEntretien p =
        plan(
            null,
            12,
            1_000,
            new PlanEntretien.DerniereRealisation(LocalDate.of(2025, 10, 20), null, 4_000));

    Echeance e = p.prochaineEcheance(0, 4_600, AUJOURDHUI, 300);

    assertThat(e.dateEcheance()).isEqualTo(LocalDate.of(2026, 10, 20));
    assertThat(e.heuresRestantes()).isEqualTo(400);
    // Échéance dans 19 jours, seuil d'alerte à 15 jours : pas encore en alerte.
    assertThat(e.etat()).isEqualTo(EtatEcheance.OK);
    assertThat(p.prochaineEcheance(0, 5_100, AUJOURDHUI, 300).etat()).isEqualTo(EtatEcheance.ECHU);
  }

  @Test
  void sansRealisationConnueLaPositionDansLeCycleSertDOrigine() {
    PlanEntretien p = plan(40_000, 12, null, null);

    Echeance e = p.prochaineEcheance(150_000, 0, AUJOURDHUI, 250);

    assertThat(e.kmRestant()).isEqualTo(10_000);
    assertThat(e.dateEcheance()).isEqualTo(AUJOURDHUI.plusDays(40));
  }

  @Test
  void uneRealisationPlusAncienneNeRemplacePasLaDerniere() {
    PlanEntretien p = plan(40_000, null, null, null);
    p.enregistrerRealisation(AUJOURDHUI, 120_000, null);
    p.enregistrerRealisation(AUJOURDHUI.minusDays(10), 118_000, null);

    assertThat(p.derniereRealisation().kilometrage()).isEqualTo(120_000);
  }

  @Test
  void auMoinsUnePeriodicite() {
    assertThatThrownBy(() -> plan(null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
