package com.logiflow.tms.maintenance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.maintenance.domain.model.NatureIntervention;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.OrigineOT;
import com.logiflow.tms.maintenance.domain.model.PrioriteOT;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.domain.model.TypeLigneCout;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.maintenance.domain.vo.LigneCout;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrdreTravailTest {

  private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 10, 1, 8, 0);

  private static OrdreTravail.DetailsOT details() {
    return new OrdreTravail.DetailsOT(
        TypeIntervention.ENTRETIEN_PREVENTIF,
        NatureIntervention.PREVENTIF,
        PrioriteOT.NORMALE,
        "Révision 40 000 km",
        null,
        null,
        DEBUT,
        DEBUT.plusHours(4),
        true,
        null);
  }

  private static OrdreTravail ot(EnginRef engin) {
    return OrdreTravail.creer(
        UUID.randomUUID(),
        Reference.generer("OT", 2026, 1),
        engin,
        OrigineOT.MANUELLE,
        null,
        null,
        details());
  }

  private static LigneCout ligne(TypeLigneCout type, String quantite, String pu, String tva) {
    return new LigneCout(
        type,
        "Ligne",
        null,
        new BigDecimal(quantite),
        new Money(new BigDecimal(pu), OrdreTravail.DEVISE),
        new BigDecimal(tva));
  }

  private static OrdreTravail.Cloture cloture(Integer km) {
    return new OrdreTravail.Cloture(
        DEBUT.plusHours(5),
        km,
        3200,
        null,
        "Vidange, filtres",
        "Atelier",
        "F-001",
        LocalDate.of(2026, 10, 2));
  }

  @Test
  void totauxHtTvaEtTtcDesLignes() {
    OrdreTravail ot = ot(EnginRef.vehicule(UUID.randomUUID()));
    ot.remplacerLignes(
        List.of(
            ligne(TypeLigneCout.MAIN_OEUVRE, "2.5", "60", "20"),
            ligne(TypeLigneCout.PIECE, "4", "12.50", "20"),
            ligne(TypeLigneCout.DIVERS, "1", "10", "0")));

    assertThat(ot.totalHt().montant()).isEqualByComparingTo("210.00");
    assertThat(ot.totalTva().montant()).isEqualByComparingTo("40.00");
    assertThat(ot.totalTtc().montant()).isEqualByComparingTo("250.00");
  }

  @Test
  void cycleCompletJusquALaClotureAvecDureeDImmobilisation() {
    OrdreTravail ot = ot(EnginRef.vehicule(UUID.randomUUID()));
    ot.remplacerLignes(List.of(ligne(TypeLigneCout.MAIN_OEUVRE, "1", "60", "20")));

    ot.changerStatut(StatutOT.EN_COURS, DEBUT);
    ot.changerStatut(StatutOT.EN_ATTENTE_PIECES, DEBUT.plusHours(1));
    ot.changerStatut(StatutOT.EN_COURS, DEBUT.plusHours(2));
    ot.cloturer(cloture(152_000));

    assertThat(ot.statut()).isEqualTo(StatutOT.TERMINE);
    assertThat(ot.realisation().debutReel()).isEqualTo(DEBUT);
    assertThat(ot.immobilisationHeures()).isEqualTo(5);
    assertThat(ot.realisation().kilometrage()).isEqualTo(152_000);
    assertThatThrownBy(() -> ot.remplacerLignes(List.of())).isInstanceOf(BusinessException.class);
  }

  @Test
  void clotureExigeLeKilometrageDUnVehiculeEtUneLigneDeCout() {
    OrdreTravail vehicule = ot(EnginRef.vehicule(UUID.randomUUID()));
    vehicule.changerStatut(StatutOT.EN_COURS, DEBUT);
    vehicule.remplacerLignes(List.of(ligne(TypeLigneCout.PIECE, "1", "10", "20")));
    assertThatThrownBy(() -> vehicule.cloturer(cloture(null))).hasMessageContaining("kilométrage");

    OrdreTravail remorque = ot(EnginRef.remorque(UUID.randomUUID()));
    remorque.changerStatut(StatutOT.EN_COURS, DEBUT);
    assertThatThrownBy(() -> remorque.cloturer(cloture(null)))
        .hasMessageContaining("ligne de coût");
    remorque.remplacerLignes(List.of(ligne(TypeLigneCout.PIECE, "1", "10", "20")));
    remorque.cloturer(cloture(null));
    assertThat(remorque.statut()).isEqualTo(StatutOT.TERMINE);
  }

  @Test
  void transitionsInterdites() {
    OrdreTravail ot = ot(EnginRef.vehicule(UUID.randomUUID()));
    assertThatThrownBy(() -> ot.changerStatut(StatutOT.TERMINE, DEBUT))
        .hasMessageContaining("clôture");
    assertThatThrownBy(() -> ot.changerStatut(StatutOT.EN_ATTENTE_PIECES, DEBUT))
        .isInstanceOf(BusinessException.class);
    assertThatThrownBy(() -> ot.cloturer(cloture(1))).hasMessageContaining("en cours");
    ot.changerStatut(StatutOT.ANNULE, DEBUT);
    assertThatThrownBy(() -> ot.modifier(details())).isInstanceOf(BusinessException.class);
  }

  @Test
  void unOtIssuDUnPlanOuDUnSinistreDoitLeReferencer() {
    assertThatThrownBy(
            () ->
                OrdreTravail.creer(
                    UUID.randomUUID(),
                    Reference.generer("OT", 2026, 2),
                    EnginRef.vehicule(UUID.randomUUID()),
                    OrigineOT.PLAN_ENTRETIEN,
                    null,
                    null,
                    details()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
