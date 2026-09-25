package com.logiflow.tms.maintenance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.maintenance.domain.model.ContratAssurance;
import com.logiflow.tms.maintenance.domain.model.Garantie;
import com.logiflow.tms.maintenance.domain.model.GraviteSinistre;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.model.StatutSinistre;
import com.logiflow.tms.maintenance.domain.model.TypeContrat;
import com.logiflow.tms.maintenance.domain.model.TypeSinistre;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Money;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SinistreEtAssuranceTest {

  // Jeudi 1er octobre 2026.
  private static final LocalDateTime SURVENANCE = LocalDateTime.of(2026, 10, 1, 14, 30);

  private static Money eur(String montant) {
    return new Money(new BigDecimal(montant), OrdreTravail.DEVISE);
  }

  private static Sinistre sinistre(Sinistre.SuiviAssurance suivi) {
    return Sinistre.declarer(
        UUID.randomUUID(),
        Reference.generer("SIN", 2026, 1),
        new Sinistre.Circonstances(
            UUID.randomUUID(),
            null,
            null,
            null,
            SURVENANCE,
            "A7, sortie 23",
            null,
            TypeSinistre.ACCROCHAGE,
            GraviteSinistre.MATERIEL_LEGER,
            null,
            "Accrochage en manœuvre",
            true,
            false,
            false,
            false,
            null),
        suivi);
  }

  @Test
  void declarationAssureurEnRetardAuDelaDeCinqJoursOuvres() {
    Sinistre s = sinistre(Sinistre.SuiviAssurance.vide());

    // Ven. 2, lun. 5 … jeu. 8 : 5 jours ouvrés, pas encore en retard ; ven. 9 : 6 jours.
    assertThat(s.declarationEnRetard(LocalDate.of(2026, 10, 8))).isFalse();
    assertThat(s.declarationEnRetard(LocalDate.of(2026, 10, 9))).isTrue();
  }

  @Test
  void workflowEtClotureConditionneeAuxReparations() {
    Sinistre s = sinistre(Sinistre.SuiviAssurance.vide());
    assertThatThrownBy(() -> s.changerStatut(StatutSinistre.DECLARE_ASSUREUR, LocalDate.now(), 0))
        .hasMessageContaining("date de déclaration");

    s.modifier(
        s.circonstances(),
        new Sinistre.SuiviAssurance(
            null, "DOS-1", LocalDate.of(2026, 10, 2), null, null, null, null, null));
    s.changerStatut(StatutSinistre.DECLARE_ASSUREUR, LocalDate.now(), 0);
    s.changerStatut(StatutSinistre.EN_REPARATION, LocalDate.now(), 0);
    assertThatThrownBy(() -> s.changerStatut(StatutSinistre.CLOS, LocalDate.now(), 1))
        .isInstanceOf(BusinessException.class);
    s.changerStatut(StatutSinistre.CLOS, LocalDate.of(2026, 11, 3), 0);

    assertThat(s.dateCloture()).isEqualTo(LocalDate.of(2026, 11, 3));
    assertThat(s.declarationEnRetard(LocalDate.of(2026, 12, 1))).isFalse();
  }

  @Test
  void coutNetDeduitLIndemnite() {
    Sinistre s =
        sinistre(
            new Sinistre.SuiviAssurance(
                null, null, null, null, null, null, eur("500"), eur("1800")));

    assertThat(s.coutNet(eur("2300")).montant()).isEqualByComparingTo("500.00");
  }

  @Test
  void contratDeFlotteEtContratDedie() {
    EnginRef frigo = EnginRef.remorque(UUID.randomUUID());
    ContratAssurance flotte =
        ContratAssurance.creer(
            UUID.randomUUID(),
            new ContratAssurance.Conditions(
                UUID.randomUUID(),
                "POL-FLOTTE",
                TypeContrat.FLOTTE,
                Set.of(Garantie.RC),
                eur("1000"),
                null,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                null,
                true));
    ContratAssurance dedie =
        ContratAssurance.creer(
            UUID.randomUUID(),
            new ContratAssurance.Conditions(
                UUID.randomUUID(),
                "POL-FRIGO",
                TypeContrat.ENGIN,
                Set.of(Garantie.DOMMAGES),
                eur("300"),
                null,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                List.of(frigo),
                true));

    assertThat(flotte.couvre(EnginRef.vehicule(UUID.randomUUID()), LocalDate.of(2026, 6, 1)))
        .isTrue();
    assertThat(flotte.couvre(frigo, LocalDate.of(2027, 1, 1))).isFalse();
    assertThat(dedie.couvre(frigo, LocalDate.of(2026, 6, 1))).isTrue();
    assertThat(dedie.specificite()).isGreaterThan(flotte.specificite());
    assertThatThrownBy(
            () ->
                new ContratAssurance.Conditions(
                    UUID.randomUUID(),
                    "X",
                    TypeContrat.ENGIN,
                    Set.of(Garantie.RC),
                    null,
                    null,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31),
                    List.of(),
                    true))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
