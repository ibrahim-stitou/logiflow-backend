package com.logiflow.tms.referential.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logiflow.tms.referential.domain.model.Horaires;
import com.logiflow.tms.referential.domain.model.Site;
import com.logiflow.tms.referential.domain.vo.ContraintesAcces;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SiteTest {

  private static final GeoPoint PARIS = new GeoPoint(48.8566, 2.3522);

  @Test
  void creerUnSiteNormaliseLeCodeEnMajuscules() {
    Site site =
        Site.creer(
            UUID.randomUUID(), " site-paris ", "Entrepôt Paris", null, PARIS, null, null, null);

    assertThat(site.code()).isEqualTo("SITE-PARIS");
    assertThat(site.estActif()).isTrue();
  }

  @Test
  void creerUnSiteAvecUnCodeVideEchoue() {
    assertThatThrownBy(
            () -> Site.creer(UUID.randomUUID(), "   ", "Entrepôt", null, PARIS, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("code");
  }

  @Test
  void creerUnSiteAvecUnLibelleVideEchoue() {
    assertThatThrownBy(
            () -> Site.creer(UUID.randomUUID(), "SITE-01", "  ", null, PARIS, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("libellé");
  }

  @Test
  void creerUnSiteSansLocalisationEchoue() {
    assertThatThrownBy(
            () ->
                Site.creer(UUID.randomUUID(), "SITE-01", "Entrepôt", null, null, null, null, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void desactiverPuisReactiverChangeLEtatDuSite() {
    Site site = Site.creer(UUID.randomUUID(), "SITE-01", "Entrepôt", null, PARIS, null, null, null);

    site.desactiver();
    assertThat(site.estActif()).isFalse();

    site.activer();
    assertThat(site.estActif()).isTrue();
  }

  @Test
  void unSiteInterditAuxPoidsLourdsNEstJamaisAccessible() {
    ContraintesAcces contraintes = new ContraintesAcces(null, null, true, null);
    Site site =
        Site.creer(UUID.randomUUID(), "SITE-01", "Entrepôt", null, PARIS, null, null, contraintes);

    assertThat(site.accessiblePour(1000)).isFalse();
  }

  @Test
  void unSiteAvecUnPoidsMaxNAcceptePasUnVehiculeTropLourd() {
    ContraintesAcces contraintes = new ContraintesAcces(null, 3.5, false, null);
    Site site =
        Site.creer(UUID.randomUUID(), "SITE-01", "Entrepôt", null, PARIS, null, null, contraintes);

    assertThat(site.accessiblePour(3000)).isTrue();
    assertThat(site.accessiblePour(4000)).isFalse();
  }

  @Test
  void modifierUnSiteMetAJourSesAttributsMutables() {
    Site site = Site.creer(UUID.randomUUID(), "SITE-01", "Entrepôt", null, PARIS, null, null, null);
    GeoPoint lyon = new GeoPoint(45.7640, 4.8357);

    site.modifier(
        "Entrepôt Lyon",
        null,
        lyon,
        "Zone industrielle",
        Horaires.aucun(),
        ContraintesAcces.aucune());

    assertThat(site.libelle()).isEqualTo("Entrepôt Lyon");
    assertThat(site.localisation()).isEqualTo(lyon);
    assertThat(site.adresse()).isEqualTo("Zone industrielle");
  }
}
