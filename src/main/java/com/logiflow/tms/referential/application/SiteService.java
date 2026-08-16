package com.logiflow.tms.referential.application;

import com.logiflow.tms.referential.api.SiteApi;
import com.logiflow.tms.referential.api.dto.SiteSummary;
import com.logiflow.tms.referential.application.command.CreerSiteCommand;
import com.logiflow.tms.referential.application.command.MajSiteCommand;
import com.logiflow.tms.referential.domain.model.Horaires;
import com.logiflow.tms.referential.domain.model.Site;
import com.logiflow.tms.referential.domain.port.out.SiteRepository;
import com.logiflow.tms.referential.domain.service.SiteDomainService;
import com.logiflow.tms.shared.application.Page;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'utilisation applicatifs du sous-domaine Site. */
@Service
@RequiredArgsConstructor
public class SiteService implements SiteApi {

  private final SiteRepository siteRepository;
  private final SiteDomainService siteDomainService;

  @Transactional
  public UUID creerSite(CreerSiteCommand command) {
    siteDomainService.verifierCodeDisponible(
        command.code(), siteRepository.existeParCode(command.code()));
    Site site =
        Site.creer(
            UUID.randomUUID(),
            command.code(),
            command.libelle(),
            command.clientId(),
            command.localisation(),
            command.adresse(),
            versHoraires(command.creneaux()),
            command.contraintesAcces());
    return siteRepository.sauvegarder(site).id();
  }

  @Transactional
  public void modifierSite(UUID id, MajSiteCommand command) {
    Site site = trouverOuEchouer(id);
    site.modifier(
        command.libelle(),
        command.clientId(),
        command.localisation(),
        command.adresse(),
        versHoraires(command.creneaux()),
        command.contraintesAcces());
    siteRepository.sauvegarder(site);
  }

  private Horaires versHoraires(List<Horaires.CreneauHoraire> creneaux) {
    return creneaux != null ? Horaires.de(creneaux) : null;
  }

  @Transactional
  public void desactiverSite(UUID id) {
    Site site = trouverOuEchouer(id);
    site.desactiver();
    siteRepository.sauvegarder(site);
  }

  @Transactional(readOnly = true)
  public Site consulterSite(UUID id) {
    return trouverOuEchouer(id);
  }

  @Transactional(readOnly = true)
  public Page<Site> listerSites(String texteRecherche, PageRequest pageRequest) {
    return siteRepository.rechercher(texteRecherche, pageRequest);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<SiteSummary> consulter(UUID siteId) {
    return siteRepository
        .parId(siteId)
        .map(
            site ->
                new SiteSummary(
                    site.id(),
                    site.code(),
                    site.libelle(),
                    site.localisation().latitude(),
                    site.localisation().longitude(),
                    site.estActif()));
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estActif(UUID siteId) {
    return siteRepository.parId(siteId).map(Site::estActif).orElse(false);
  }

  private Site trouverOuEchouer(UUID id) {
    return siteRepository
        .parId(id)
        .orElseThrow(() -> new NotFoundException("Aucun site trouvé pour l'identifiant " + id));
  }
}
