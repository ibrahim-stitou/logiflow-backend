package com.logiflow.tms.planning.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.StatutVoyage;
import com.logiflow.tms.planning.domain.model.TypeEtape;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Etape;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VoyageTest {

  private static final Reference REFERENCE = Reference.generer("VOY", 2026, 1);

  private static Trajet trajetNominal(Instant depart) {
    return new Trajet(
        450,
        360,
        420,
        List.of(
            new Etape(0, TypeEtape.CHARGEMENT, depart, depart.plus(1, ChronoUnit.HOURS), 0, 500),
            new Etape(1, TypeEtape.DECHARGEMENT, depart.plus(7, ChronoUnit.HOURS), null, 450, 0)));
  }

  private static Voyage voyageNominal() {
    Instant depart = Instant.now();
    return Voyage.creer(
        UUID.randomUUID(),
        REFERENCE,
        TypeVoyage.SIMPLE,
        Portee.NATIONAL,
        depart,
        depart.plus(8, ChronoUnit.HOURS),
        UUID.randomUUID(),
        UUID.randomUUID(),
        List.of(UUID.randomUUID()),
        trajetNominal(depart),
        List.of(new Affectation(UUID.randomUUID(), RoleChauffeur.TITULAIRE, depart)),
        0.75);
  }

  @Test
  void creerUnVoyageEstAuStatutBrouillon() {
    Voyage voyage = voyageNominal();

    assertThat(voyage.statut()).isEqualTo(StatutVoyage.BROUILLON);
  }

  @Test
  void suivreLeCycleDeVieNominalFonctionne() {
    Voyage voyage = voyageNominal();

    voyage.changerStatut(StatutVoyage.PLANIFIE);
    voyage.changerStatut(StatutVoyage.AFFECTE);
    voyage.changerStatut(StatutVoyage.EN_COURS);
    voyage.changerStatut(StatutVoyage.TERMINE);
    voyage.changerStatut(StatutVoyage.CLOTURE);

    assertThat(voyage.statut()).isEqualTo(StatutVoyage.CLOTURE);
  }

  @Test
  void uneTransitionInterditeEchoue() {
    Voyage voyage = voyageNominal();

    assertThatThrownBy(() -> voyage.changerStatut(StatutVoyage.EN_COURS))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void unVoyageSansAffectationEchoueALaCreation() {
    Instant depart = Instant.now();
    assertThatThrownBy(
            () ->
                Voyage.creer(
                    UUID.randomUUID(),
                    REFERENCE,
                    TypeVoyage.SIMPLE,
                    Portee.NATIONAL,
                    depart,
                    depart.plus(8, ChronoUnit.HOURS),
                    UUID.randomUUID(),
                    null,
                    List.of(UUID.randomUUID()),
                    trajetNominal(depart),
                    List.of(),
                    0.5))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void unVoyageAvecPlusieursDossiersEstUnGroupage() {
    Instant depart = Instant.now();
    Voyage voyage =
        Voyage.creer(
            UUID.randomUUID(),
            REFERENCE,
            TypeVoyage.GROUPAGE,
            Portee.NATIONAL,
            depart,
            depart.plus(8, ChronoUnit.HOURS),
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(UUID.randomUUID(), UUID.randomUUID()),
            trajetNominal(depart),
            List.of(new Affectation(UUID.randomUUID(), RoleChauffeur.TITULAIRE, depart)),
            0.9);

    assertThat(voyage.estGroupage()).isTrue();
  }

  @Test
  void mettreAJourUnTauxDeRemplissageInvalideEchoue() {
    Voyage voyage = voyageNominal();

    assertThatThrownBy(() -> voyage.mettreAJourRemplissage(1.5))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
