package com.logiflow.tms.planning.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.logiflow.tms.planning.application.DeviationItineraireService.ResultatVerificationDeviation;
import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.model.Portee;
import com.logiflow.tms.planning.domain.model.RoleChauffeur;
import com.logiflow.tms.planning.domain.model.TypeEtape;
import com.logiflow.tms.planning.domain.model.TypeVoyage;
import com.logiflow.tms.planning.domain.model.Voyage;
import com.logiflow.tms.planning.domain.port.out.VoyageArretRepository;
import com.logiflow.tms.planning.domain.port.out.VoyageRepository;
import com.logiflow.tms.planning.domain.service.InsertionItineraireDomainService;
import com.logiflow.tms.config.LogiflowProperties;
import com.logiflow.tms.planning.domain.vo.Affectation;
import com.logiflow.tms.planning.domain.vo.Etape;
import com.logiflow.tms.planning.domain.vo.Trajet;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.shared.domain.vo.Reference;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviationItineraireServiceTest {

  @Mock private VoyageRepository voyageRepository;
  @Mock private VoyageArretRepository voyageArretRepository;

  private DeviationItineraireService deviationItineraireService;
  private UUID voyageId;

  @BeforeEach
  void setUp() {
    deviationItineraireService =
        new DeviationItineraireService(
            voyageRepository,
            voyageArretRepository,
            new InsertionItineraireDomainService(),
            proprietesAvecDeviationMax(25));
    voyageId = UUID.randomUUID();
    preparerVoyage();
  }

  @Test
  void verifierDeviationAccepteDesPointsProchesDeLItineraire() {
    ResultatVerificationDeviation resultat =
        deviationItineraireService.verifierDeviation(
            voyageId, new GeoPoint(0, 0.5), new GeoPoint(0, 1.5), null);

    assertThat(resultat.accepte()).isTrue();
    assertThat(resultat.raisonsRejet()).isEmpty();
  }

  @Test
  void verifierDeviationRejetteUnDetourExcessif() {
    ResultatVerificationDeviation resultat =
        deviationItineraireService.verifierDeviation(
            voyageId, new GeoPoint(0.5, 0.5), new GeoPoint(0, 1.5), 25d);

    assertThat(resultat.accepte()).isFalse();
    assertThat(resultat.raisonsRejet())
        .anyMatch(r -> r.contains("Détour de chargement"));
  }

  @Test
  void verifierDeviationRejetteUnDechargementAvantLeChargement() {
    ResultatVerificationDeviation resultat =
        deviationItineraireService.verifierDeviation(
            voyageId, new GeoPoint(0, 1.5), new GeoPoint(0, 0.5), 80d);

    assertThat(resultat.accepte()).isFalse();
    assertThat(resultat.raisonsRejet())
        .anyMatch(r -> r.contains("ne peut pas être inséré avant le point de chargement"));
    assertThat(resultat.chargement().apresIndiceArret())
        .isGreaterThan(resultat.dechargement().apresIndiceArret());
  }

  @Test
  void trouverMeilleureInsertionRetourneLeTronconOptimal() {
    var resultat =
        deviationItineraireService.trouverMeilleureInsertion(voyageId, new GeoPoint(0, 1.5));

    assertThat(resultat.apresIndiceArret()).isEqualTo(1);
    assertThat(resultat.detourKm()).isGreaterThanOrEqualTo(0);
  }

  private void preparerVoyage() {
    Instant depart = Instant.now();
    Trajet trajet =
        new Trajet(
            450,
            360,
            420,
            List.of(
                new Etape(
                    0, TypeEtape.CHARGEMENT, depart, depart.plus(1, ChronoUnit.HOURS), 0, 500),
                new Etape(
                    1, TypeEtape.DECHARGEMENT, depart.plus(7, ChronoUnit.HOURS), null, 450, 0)));

    Voyage voyage =
        Voyage.creer(
            voyageId,
            Reference.generer("VOY", 2026, 1),
            TypeVoyage.SIMPLE,
            Portee.NATIONAL,
            depart,
            depart.plus(8, ChronoUnit.HOURS),
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(UUID.randomUUID()),
            trajet,
            List.of(new Affectation(UUID.randomUUID(), RoleChauffeur.TITULAIRE, depart)),
            0.5);

    when(voyageRepository.parId(voyageId)).thenReturn(Optional.of(voyage));
    when(voyageArretRepository.parVoyageIdOrdonnes(voyageId))
        .thenReturn(
            List.of(
                arret(0, "A", 0, 0),
                arret(1, "B", 0, 1),
                arret(2, "C", 0, 2)));
  }

  private ArretVoyage arret(int indice, String libelle, double lat, double lon) {
    return ArretVoyage.creer(
        UUID.randomUUID(), voyageId, indice, libelle, new GeoPoint(lat, lon), null, true);
  }

  private static LogiflowProperties proprietesAvecDeviationMax(double deviationMaxPourcent) {
    return new LogiflowProperties(
        new LogiflowProperties.Cors(List.of("http://localhost:4200")),
        new LogiflowProperties.Security(false, false, null),
        new LogiflowProperties.Planning(deviationMaxPourcent));
  }
}
