package com.logiflow.tms.tracking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.tracking.domain.model.EvenementVoyage;
import com.logiflow.tms.tracking.domain.model.TypeEvenement;
import com.logiflow.tms.tracking.domain.service.TrackingDomainService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvenementVoyageTest {

  @Test
  void declarerUnEvenementAvecPositionEstGeolocalise() {
    EvenementVoyage evenement =
        EvenementVoyage.declarer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TypeEvenement.DEPART,
            Instant.now(),
            new GeoPoint(48.8566, 2.3522),
            null);

    assertThat(evenement.estGeolocalise()).isTrue();
  }

  @Test
  void declarerUnEvenementSansPositionNEstPasGeolocalise() {
    EvenementVoyage evenement =
        EvenementVoyage.declarer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            TypeEvenement.INCIDENT,
            Instant.now(),
            null,
            "Panne moteur");

    assertThat(evenement.estGeolocalise()).isFalse();
  }

  @Test
  void unEvenementAnterieurAuDernierEnregistreEstRefuse() {
    TrackingDomainService service = new TrackingDomainService();
    Instant dernier = Instant.now();
    Instant nouveau = dernier.minus(1, ChronoUnit.HOURS);

    assertThatThrownBy(() -> service.verifierChronologie(nouveau, Optional.of(dernier)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void unEvenementPosterieurAuDernierEstAccepte() {
    TrackingDomainService service = new TrackingDomainService();
    Instant dernier = Instant.now();
    Instant nouveau = dernier.plus(1, ChronoUnit.HOURS);

    service.verifierChronologie(nouveau, Optional.of(dernier));
  }
}
