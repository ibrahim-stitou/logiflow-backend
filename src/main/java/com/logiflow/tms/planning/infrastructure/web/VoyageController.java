package com.logiflow.tms.planning.infrastructure.web;

import com.logiflow.tms.planning.application.DisponibiliteRessourcesService;
import com.logiflow.tms.planning.application.DisponibiliteRessourcesService.RessourcesDisponibles;
import com.logiflow.tms.planning.application.VoyageCapaciteService;
import com.logiflow.tms.planning.application.VoyageDossierService;
import com.logiflow.tms.planning.application.VoyageService;
import com.logiflow.tms.planning.domain.model.StatutVoyage;
import com.logiflow.tms.planning.infrastructure.web.dto.AjouterDossierVoyageRequest;
import com.logiflow.tms.planning.infrastructure.web.dto.ArretVoyageResponse;
import com.logiflow.tms.planning.infrastructure.web.dto.ConformiteVoyageRequest;
import com.logiflow.tms.planning.infrastructure.web.dto.ConformiteVoyageResponse;
import com.logiflow.tms.planning.infrastructure.web.dto.VerifierAjoutDossierResponse;
import com.logiflow.tms.planning.infrastructure.web.dto.VoyageCapaciteResponse;
import com.logiflow.tms.planning.infrastructure.web.dto.VoyageRequest;
import com.logiflow.tms.planning.infrastructure.web.dto.VoyageResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST du sous-domaine Voyage. */
@RestController
@RequiredArgsConstructor
public class VoyageController {

  private final VoyageService voyageService;
  private final VoyageDossierService voyageDossierService;
  private final VoyageCapaciteService voyageCapaciteService;
  private final DisponibiliteRessourcesService disponibiliteRessourcesService;

  @PostMapping("/api/v1/voyages")
  public ResponseEntity<VoyageResponse> creer(@Valid @RequestBody VoyageRequest request) {
    UUID id = voyageService.creerVoyage(request.versCommande());
    VoyageResponse reponse = VoyageResponse.depuis(voyageService.consulterVoyage(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  /** Contrôle de conformité à blanc d'un projet de voyage (aucune écriture). */
  @PostMapping("/api/v1/voyages/conformite")
  public ConformiteVoyageResponse evaluerConformite(
      @Valid @RequestBody ConformiteVoyageRequest request) {
    return ConformiteVoyageResponse.depuis(voyageService.evaluerConformite(request.versCommande()));
  }

  /** Véhicules, remorques et chauffeurs libres et exploitables sur [debut, fin]. */
  @GetMapping("/api/v1/voyages/ressources-disponibles")
  public RessourcesDisponibles ressourcesDisponibles(
      @RequestParam Instant debut, @RequestParam Instant fin) {
    return disponibiliteRessourcesService.ressourcesDisponibles(debut, fin);
  }

  @GetMapping("/api/v1/voyages/{id}/arrets")
  public List<ArretVoyageResponse> listerArrets(@PathVariable UUID id) {
    return voyageService.listerArrets(id).stream().map(ArretVoyageResponse::depuis).toList();
  }

  @GetMapping("/api/v1/voyages/{id}")
  public VoyageResponse consulter(@PathVariable UUID id) {
    return VoyageResponse.depuis(voyageService.consulterVoyage(id));
  }

  @GetMapping("/api/v1/voyages/{id}/capacite")
  public VoyageCapaciteResponse consulterCapacite(@PathVariable UUID id) {
    return VoyageCapaciteResponse.depuis(voyageCapaciteService.obtenirVueCapacite(id));
  }

  @GetMapping("/api/v1/voyages")
  public PageResponse<VoyageResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID dossierId,
      @RequestParam(required = false) UUID chauffeurId) {
    if (chauffeurId != null) {
      var voyages = voyageService.listerParChauffeur(chauffeurId);
      return PageResponse.ofList(voyages.stream().map(VoyageResponse::depuis).toList());
    }
    if (dossierId != null) {
      var voyages = voyageService.listerParDossier(dossierId);
      return PageResponse.ofList(voyages.stream().map(VoyageResponse::depuis).toList());
    }
    var resultats = voyageService.listerVoyages(q, new PageRequest(page, size));
    return PageResponse.of(resultats, VoyageResponse::depuis);
  }

  @PutMapping("/api/v1/voyages/{id}/statut")
  public VoyageResponse changerStatut(@PathVariable UUID id, @RequestParam StatutVoyage valeur) {
    voyageService.changerStatut(id, valeur);
    return VoyageResponse.depuis(voyageService.consulterVoyage(id));
  }

  @PostMapping("/api/v1/voyages/{voyageId}/dossiers/check")
  public VerifierAjoutDossierResponse verifierAjoutDossier(
      @PathVariable UUID voyageId, @Valid @RequestBody AjouterDossierVoyageRequest request) {
    return VerifierAjoutDossierResponse.depuis(
        voyageDossierService.verifierAjoutDossier(voyageId, request.versCommande()));
  }

  @PostMapping("/api/v1/voyages/{voyageId}/dossiers")
  public ResponseEntity<Void> ajouterDossier(
      @PathVariable UUID voyageId, @Valid @RequestBody AjouterDossierVoyageRequest request) {
    UUID dossierId = voyageDossierService.ajouterDossier(voyageId, request.versCommande());
    URI location =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/v1/dossiers/{id}")
            .buildAndExpand(dossierId)
            .toUri();
    return ResponseEntity.created(location).build();
  }
}
