package com.logiflow.tms.maintenance.infrastructure.web;

import com.logiflow.tms.maintenance.application.PrestataireService;
import com.logiflow.tms.maintenance.domain.model.Prestataire;
import com.logiflow.tms.maintenance.domain.model.TypePrestataire;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST des prestataires (garages, assureurs, experts…). */
@RestController
@RequestMapping("/api/v1/maintenance/prestataires")
@RequiredArgsConstructor
public class PrestataireController {

  private final PrestataireService service;

  public record PrestataireRequest(
      String code,
      @NotBlank String raisonSociale,
      @NotNull TypePrestataire type,
      String siret,
      String contactNom,
      String telephone,
      @Email String email,
      String adresse,
      String notes,
      Boolean actif) {

    Prestataire.Fiche versFiche() {
      return new Prestataire.Fiche(
          raisonSociale,
          type,
          siret,
          contactNom,
          telephone,
          email,
          adresse,
          notes,
          actif == null || actif);
    }
  }

  public record PrestataireResponse(
      UUID id,
      String code,
      String raisonSociale,
      TypePrestataire type,
      String siret,
      String contactNom,
      String telephone,
      String email,
      String adresse,
      String notes,
      boolean actif) {

    static PrestataireResponse depuis(Prestataire p) {
      var f = p.fiche();
      return new PrestataireResponse(
          p.id(),
          p.code(),
          f.raisonSociale(),
          f.type(),
          f.siret(),
          f.contactNom(),
          f.telephone(),
          f.email(),
          f.adresse(),
          f.notes(),
          f.actif());
    }
  }

  @PostMapping
  public ResponseEntity<PrestataireResponse> creer(@Valid @RequestBody PrestataireRequest r) {
    if (r.code() == null || r.code().isBlank()) {
      throw new IllegalArgumentException("Le code du prestataire est obligatoire");
    }
    Prestataire p = service.creer(r.code(), r.versFiche());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(p.id())
            .toUri();
    return ResponseEntity.created(location).body(PrestataireResponse.depuis(p));
  }

  @GetMapping
  public PageResponse<PrestataireResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) TypePrestataire type,
      @RequestParam(required = false) Boolean actif,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return PageResponse.of(
        service.rechercher(q, type, actif, new PageRequest(page, size)),
        PrestataireResponse::depuis);
  }

  @GetMapping("/{id}")
  public PrestataireResponse consulter(@PathVariable UUID id) {
    return PrestataireResponse.depuis(service.consulter(id));
  }

  @PutMapping("/{id}")
  public PrestataireResponse modifier(
      @PathVariable UUID id, @Valid @RequestBody PrestataireRequest r) {
    return PrestataireResponse.depuis(service.modifier(id, r.versFiche()));
  }
}
