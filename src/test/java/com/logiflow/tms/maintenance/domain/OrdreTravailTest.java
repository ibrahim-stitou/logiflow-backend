package com.logiflow.tms.maintenance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrdreTravailTest {

  private static final Money COUT = new Money(BigDecimal.valueOf(350), Currency.getInstance("EUR"));

  @Test
  void creerUnOrdreTravailEstAuStatutPlanifie() {
    OrdreTravail ot =
        OrdreTravail.creer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TypeIntervention.ENTRETIEN_PREVENTIF,
            LocalDateTime.now(),
            COUT);

    assertThat(ot.statut()).isEqualTo(StatutOT.PLANIFIE);
  }

  @Test
  void cloturerUnOrdreTravailMetAJourDureeEtCout() {
    OrdreTravail ot =
        OrdreTravail.creer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TypeIntervention.REPARATION,
            LocalDateTime.now(),
            COUT);
    ot.changerStatut(StatutOT.EN_COURS);
    Money coutReel = new Money(BigDecimal.valueOf(420), Currency.getInstance("EUR"));

    ot.cloturer(90, coutReel);

    assertThat(ot.statut()).isEqualTo(StatutOT.TERMINE);
    assertThat(ot.dureeReelleMin()).isEqualTo(90);
    assertThat(ot.cout()).isEqualTo(coutReel);
  }

  @Test
  void unOrdreTravailTermineNAccepteAucuneTransition() {
    OrdreTravail ot =
        OrdreTravail.creer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TypeIntervention.PNEUS,
            LocalDateTime.now(),
            COUT);
    ot.changerStatut(StatutOT.EN_COURS);
    ot.changerStatut(StatutOT.TERMINE);

    assertThatThrownBy(() -> ot.changerStatut(StatutOT.EN_COURS))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void onNePeutPasPasserDirectementDePlanifieATermine() {
    OrdreTravail ot =
        OrdreTravail.creer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TypeIntervention.CONTROLE_TECHNIQUE,
            LocalDateTime.now(),
            COUT);

    assertThatThrownBy(() -> ot.changerStatut(StatutOT.TERMINE))
        .isInstanceOf(BusinessException.class);
  }
}
