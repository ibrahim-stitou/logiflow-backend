package com.logiflow.tms.planning.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.logiflow.tms.planning.domain.model.ArretVoyage;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService.PointsDossier;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService.Resultat;
import com.logiflow.tms.planning.domain.service.ItineraireDossiersDomainService.SitePlanifie;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ItineraireDossiersDomainServiceTest {

  private static final Instant T0 = Instant.parse("2026-10-05T06:00:00Z");

  private final ItineraireDossiersDomainService service = new ItineraireDossiersDomainService();
  private final UUID a = UUID.randomUUID();
  private final UUID b = UUID.randomUUID();
  private final UUID c = UUID.randomUUID();
  private final Map<UUID, SitePlanifie> sites =
      Map.of(
          a, new SitePlanifie(a, "A", new GeoPoint(45, 4)),
          b, new SitePlanifie(b, "B", new GeoPoint(46, 4)),
          c, new SitePlanifie(c, "C", new GeoPoint(47, 4)));

  private static PointsDossier dossier(String ref, UUID de, int hDe, UUID vers, int hVers) {
    return new PointsDossier(
        UUID.randomUUID(),
        ref,
        de,
        T0.plus(hDe, ChronoUnit.HOURS),
        vers,
        T0.plus(hVers, ChronoUnit.HOURS));
  }

  private static List<UUID> sitesDe(Resultat resultat) {
    return resultat.arrets().stream().map(ArretVoyage::siteId).toList();
  }

  @Test
  void deduitLOrdreDesFenetresEtFusionneLesPassagesConsecutifs() {
    PointsDossier d1 = dossier("D1", a, 0, b, 3);
    PointsDossier d2 = dossier("D2", b, 4, c, 6);

    Resultat resultat = service.construire(UUID.randomUUID(), List.of(d1, d2), sites, null);

    assertThat(resultat.valide()).isTrue();
    assertThat(sitesDe(resultat)).containsExactly(a, b, c);
    assertThat(resultat.arretsParDossier().get(d2.dossierId()).indiceChargement()).isEqualTo(1);
    assertThat(resultat.arretsParDossier().get(d2.dossierId()).indiceDechargement()).isEqualTo(2);
  }

  @Test
  void neDechargeJamaisAvantLeChargement() {
    PointsDossier incoherent = dossier("D1", a, 5, b, 1);

    Resultat resultat = service.construire(UUID.randomUUID(), List.of(incoherent), sites, null);

    assertThat(sitesDe(resultat)).containsExactly(a, b);
  }

  @Test
  void respecteUnOrdreImposeEtSignaleUnDechargementNonDesservi() {
    PointsDossier d1 = dossier("D1", a, 0, c, 3);
    PointsDossier d2 = dossier("D2", b, 1, a, 2);

    Resultat resultat =
        service.construire(UUID.randomUUID(), List.of(d1, d2), sites, List.of(a, b, c));

    assertThat(sitesDe(resultat)).containsExactly(a, b, c);
    assertThat(resultat.erreurs())
        .containsExactly(
            "L'itinéraire ne dessert pas le site de déchargement du dossier D2 après son chargement");
  }

  @Test
  void siteInconnuRendLItineraireInvalide() {
    PointsDossier d1 = dossier("D1", a, 0, UUID.randomUUID(), 3);

    Resultat resultat = service.construire(UUID.randomUUID(), List.of(d1), sites, null);

    assertThat(resultat.valide()).isFalse();
    assertThat(resultat.arrets()).isEmpty();
  }
}
