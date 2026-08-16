package com.logiflow.tms.referential.infrastructure.web;

import com.logiflow.tms.referential.application.SiteService;
import com.logiflow.tms.referential.application.command.CreerSiteCommand;
import com.logiflow.tms.referential.application.command.MajSiteCommand;
import com.logiflow.tms.referential.domain.model.Horaires;
import com.logiflow.tms.referential.infrastructure.web.dto.SiteRequest;
import com.logiflow.tms.referential.infrastructure.web.dto.SiteResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * API REST du sous-domaine Site. Ne contient aucune logique métier : validation du corps de
 * requête, appel du service applicatif, mapping vers les DTO publics.
 */
@RestController
@RequiredArgsConstructor
public class SiteController {

  private final SiteService siteService;

  @PostMapping("/api/v1/sites")
  public ResponseEntity<SiteResponse> creer(@Valid @RequestBody SiteRequest request) {
    UUID id = siteService.creerSite(versCommandeCreation(request));
    SiteResponse reponse = SiteResponse.depuis(siteService.consulterSite(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/sites/{id}")
  public SiteResponse consulter(@PathVariable UUID id) {
    return SiteResponse.depuis(siteService.consulterSite(id));
  }

  @GetMapping("/api/v1/sites")
  public PageResponse<SiteResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats = siteService.listerSites(q, new PageRequest(page, size));
    return PageResponse.of(resultats, SiteResponse::depuis);
  }

  @PutMapping("/api/v1/sites/{id}")
  public SiteResponse modifier(@PathVariable UUID id, @Valid @RequestBody SiteRequest request) {
    siteService.modifierSite(id, versCommandeMaj(request));
    return SiteResponse.depuis(siteService.consulterSite(id));
  }

  @DeleteMapping("/api/v1/sites/{id}")
  public ResponseEntity<Void> desactiver(@PathVariable UUID id) {
    siteService.desactiverSite(id);
    return ResponseEntity.noContent().build();
  }

  private CreerSiteCommand versCommandeCreation(SiteRequest request) {
    return new CreerSiteCommand(
        request.code(),
        request.libelle(),
        request.clientId(),
        request.localisation(),
        request.adresse(),
        request.contraintesAcces(),
        versCreneaux(request.horaires()));
  }

  private MajSiteCommand versCommandeMaj(SiteRequest request) {
    return new MajSiteCommand(
        request.libelle(),
        request.clientId(),
        request.localisation(),
        request.adresse(),
        request.contraintesAcces(),
        versCreneaux(request.horaires()));
  }

  private List<Horaires.CreneauHoraire> versCreneaux(List<SiteRequest.CreneauRequest> horaires) {
    if (horaires == null) {
      return List.of();
    }
    return horaires.stream()
        .map(creneau -> new Horaires.CreneauHoraire(creneau.jour(), creneau.debut(), creneau.fin()))
        .toList();
  }
}
