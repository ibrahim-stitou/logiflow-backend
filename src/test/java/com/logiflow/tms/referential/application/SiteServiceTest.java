package com.logiflow.tms.referential.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logiflow.tms.referential.application.command.CreerSiteCommand;
import com.logiflow.tms.referential.domain.model.Site;
import com.logiflow.tms.referential.domain.port.out.SiteRepository;
import com.logiflow.tms.referential.domain.service.SiteDomainService;
import com.logiflow.tms.shared.domain.exception.ConflictException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiteServiceTest {

  @Mock private SiteRepository siteRepository;

  private SiteService siteService;

  @BeforeEach
  void setUp() {
    siteService = new SiteService(siteRepository, new SiteDomainService());
  }

  @Test
  void creerSiteEchoueSiLeCodeEstDejaUtilise() {
    CreerSiteCommand command =
        new CreerSiteCommand(
            "SITE-01", "Entrepôt", null, new GeoPoint(48.8566, 2.3522), null, null, null);
    when(siteRepository.existeParCode("SITE-01")).thenReturn(true);

    assertThatThrownBy(() -> siteService.creerSite(command)).isInstanceOf(ConflictException.class);
  }

  @Test
  void creerSiteSauvegardeUnNouveauSiteQuandLeCodeEstDisponible() {
    CreerSiteCommand command =
        new CreerSiteCommand(
            "SITE-01", "Entrepôt", null, new GeoPoint(48.8566, 2.3522), null, null, null);
    when(siteRepository.existeParCode("SITE-01")).thenReturn(false);
    when(siteRepository.sauvegarder(any(Site.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UUID id = siteService.creerSite(command);

    assertThat(id).isNotNull();
    verify(siteRepository).sauvegarder(any(Site.class));
  }

  @Test
  void consulterUnSiteInexistantLeveNotFoundException() {
    UUID id = UUID.randomUUID();
    when(siteRepository.parId(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> siteService.consulterSite(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void desactiverUnSiteExistantLePersisteDesactive() {
    UUID id = UUID.randomUUID();
    Site site =
        Site.creer(
            id, "SITE-01", "Entrepôt", null, new GeoPoint(48.8566, 2.3522), null, null, null);
    when(siteRepository.parId(id)).thenReturn(Optional.of(site));
    when(siteRepository.sauvegarder(any(Site.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    siteService.desactiverSite(id);

    assertThat(site.estActif()).isFalse();
    verify(siteRepository).sauvegarder(site);
  }
}
