package com.logiflow.tms.maintenance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.logiflow.tms.maintenance.domain.model.ScoreSante;
import com.logiflow.tms.maintenance.domain.model.StatutSante;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ScoreSanteTest {

  @Test
  void unScoreEleveEtUneEcheanceLointaineDonneUnStatutBon() {
    ScoreSante score =
        ScoreSante.calculer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            90,
            5000,
            LocalDate.now().plusMonths(6),
            null);

    assertThat(score.statut()).isEqualTo(StatutSante.BON);
    assertThat(score.necessiteIntervention()).isFalse();
  }

  @Test
  void uneEcheanceDepasseeDonneUnStatutCritiqueQuelQueSoitLeScore() {
    ScoreSante score =
        ScoreSante.calculer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            95,
            -100,
            LocalDate.now().minusDays(1),
            null);

    assertThat(score.statut()).isEqualTo(StatutSante.CRITIQUE);
    assertThat(score.necessiteIntervention()).isTrue();
  }

  @Test
  void unScoreFaibleDonneUnStatutAPlanifier() {
    ScoreSante score =
        ScoreSante.calculer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now(),
            45,
            2000,
            LocalDate.now().plusMonths(1),
            "Vidange à prévoir");

    assertThat(score.statut()).isEqualTo(StatutSante.A_PLANIFIER);
    assertThat(score.necessiteIntervention()).isTrue();
  }
}
