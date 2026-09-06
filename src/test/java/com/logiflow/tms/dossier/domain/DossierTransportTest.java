package com.logiflow.tms.dossier.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.dossier.domain.model.DossierTransport;
import com.logiflow.tms.dossier.domain.model.StatutDossier;
import com.logiflow.tms.dossier.domain.model.TypeSegment;
import com.logiflow.tms.dossier.domain.model.TypeTransport;
import com.logiflow.tms.dossier.domain.vo.LigneMarchandise;
import com.logiflow.tms.dossier.domain.vo.Segment;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Reference;
import com.logiflow.tms.shared.domain.vo.TimeWindow;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DossierTransportTest {

  private static final Reference REFERENCE = Reference.generer("DT", 2026, 1);

  private static List<Segment> segmentsNominaux() {
    Instant maintenant = Instant.now();
    return List.of(
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
            null));
  }

  private static DossierTransport dossier(boolean groupable) {
    return DossierTransport.creer(
        UUID.randomUUID(),
        REFERENCE,
        UUID.randomUUID(),
        TypeTransport.NATIONAL,
        groupable,
        500,
        2.5,
        10,
        "Palettes standard",
        null,
        null,
        List.of(new LigneMarchandise(UUID.randomUUID(), 500, 2.5, 10, null, null, true)),
        segmentsNominaux(),
        List.of());
  }

  @Test
  void creerUnDossierEstAuStatutCree() {
    DossierTransport dossier = dossier(true);

    assertThat(dossier.statut()).isEqualTo(StatutDossier.CREE);
  }

  @Test
  void creerUnDossierAvecMoinsDeDeuxSegmentsEchoue() {
    Instant maintenant = Instant.now();
    List<Segment> unSeulSegment =
        List.of(
            new Segment(
                TypeSegment.CHARGEMENT,
                0,
                UUID.randomUUID(),
                new TimeWindow(maintenant, maintenant.plus(2, ChronoUnit.HOURS)),
                null));

    assertThatThrownBy(
            () ->
                DossierTransport.creer(
                    UUID.randomUUID(),
                    REFERENCE,
                    UUID.randomUUID(),
                    TypeTransport.NATIONAL,
                    true,
                    500,
                    2.5,
                    10,
                    "Palettes",
                    null,
                    null,
                    List.of(new LigneMarchandise(UUID.randomUUID(), 500, 2.5, 10, null, null, true)),
                    unSeulSegment,
                    List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void suivreLeCycleDeVieNominalFonctionne() {
    DossierTransport dossier = dossier(true);

    dossier.changerStatut(StatutDossier.PLANIFIE);
    dossier.changerStatut(StatutDossier.EN_CHARGEMENT);
    dossier.changerStatut(StatutDossier.CHARGE);
    dossier.changerStatut(StatutDossier.EN_TRANSIT);
    dossier.changerStatut(StatutDossier.EN_LIVRAISON);
    dossier.changerStatut(StatutDossier.LIVRE);
    dossier.changerStatut(StatutDossier.CLOTURE);

    assertThat(dossier.statut()).isEqualTo(StatutDossier.CLOTURE);
  }

  @Test
  void uneTransitionInterditeEchoue() {
    DossierTransport dossier = dossier(true);

    assertThatThrownBy(() -> dossier.changerStatut(StatutDossier.LIVRE))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void unDossierClotureNAccepteAucuneNouvelleTransition() {
    DossierTransport dossier = dossier(true);
    dossier.changerStatut(StatutDossier.PLANIFIE);
    dossier.changerStatut(StatutDossier.EN_CHARGEMENT);
    dossier.changerStatut(StatutDossier.CHARGE);
    dossier.changerStatut(StatutDossier.EN_TRANSIT);
    dossier.changerStatut(StatutDossier.EN_LIVRAISON);
    dossier.changerStatut(StatutDossier.LIVRE);
    dossier.changerStatut(StatutDossier.CLOTURE);

    assertThatThrownBy(() -> dossier.changerStatut(StatutDossier.ANNULE))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void deuxDossiersGroupablesDeMemeTypeSontCompatibles() {
    assertThat(dossier(true).estGroupableAvec(dossier(true))).isTrue();
  }

  @Test
  void unDossierNonGroupableNEstJamaisCompatible() {
    assertThat(dossier(false).estGroupableAvec(dossier(true))).isFalse();
  }

  @Test
  void capaciteRequiseRefleteLePoidsLeVolumeEtLesPalettes() {
    DossierTransport dossier = dossier(true);

    var capacite = dossier.capaciteRequise();

    assertThat(capacite.poidsKg()).isEqualTo(500);
    assertThat(capacite.volumeM3()).isEqualTo(2.5);
    assertThat(capacite.positionsPalettes()).isEqualTo(10);
  }
}
