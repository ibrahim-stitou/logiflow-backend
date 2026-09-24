package com.logiflow.tms.driver.infrastructure.web;

import com.logiflow.tms.driver.application.ChauffeurService;
import com.logiflow.tms.driver.application.command.CreerChauffeurCommand;
import com.logiflow.tms.driver.application.command.MajChauffeurCommand;
import com.logiflow.tms.driver.domain.model.DisponibiliteChauffeur;
import com.logiflow.tms.driver.domain.model.ProfilChauffeur;
import com.logiflow.tms.driver.domain.model.StatutChauffeur;
import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurMajRequest;
import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurRequest;
import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurResponse;
import com.logiflow.tms.driver.infrastructure.web.dto.ProfilChauffeurRequest;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST du sous-domaine Chauffeur. */
@RestController
@RequiredArgsConstructor
public class ChauffeurController {

  private final ChauffeurService chauffeurService;

  public record StatutRequest(@NotNull StatutChauffeur valeur) {}

  public record DisponibiliteRequest(@NotNull DisponibiliteChauffeur valeur) {}

  @PostMapping("/api/v1/chauffeurs")
  public ResponseEntity<ChauffeurResponse> creer(@Valid @RequestBody ChauffeurRequest request) {
    UUID id =
        chauffeurService.creerChauffeur(
            new CreerChauffeurCommand(
                request.matricule(),
                request.nom(),
                request.prenom(),
                profil(request.profil()),
                habilitations(request.habilitations()),
                request.soldeTempsConduiteInitialMinutes()));
    ChauffeurResponse reponse = ChauffeurResponse.depuis(chauffeurService.consulterChauffeur(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/chauffeurs/{id}")
  public ChauffeurResponse consulter(@PathVariable UUID id) {
    return ChauffeurResponse.depuis(chauffeurService.consulterChauffeur(id));
  }

  @GetMapping("/api/v1/chauffeurs")
  public PageResponse<ChauffeurResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) StatutChauffeur statut,
      @RequestParam(required = false) DisponibiliteChauffeur disponibilite,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats =
        chauffeurService.listerChauffeurs(q, statut, disponibilite, new PageRequest(page, size));
    return PageResponse.of(resultats, ChauffeurResponse::depuis);
  }

  @PutMapping("/api/v1/chauffeurs/{id}")
  public ChauffeurResponse modifier(
      @PathVariable UUID id, @Valid @RequestBody ChauffeurMajRequest request) {
    chauffeurService.modifierChauffeur(
        id,
        new MajChauffeurCommand(
            request.nom(),
            request.prenom(),
            profil(request.profil()),
            habilitations(request.habilitations())));
    return ChauffeurResponse.depuis(chauffeurService.consulterChauffeur(id));
  }

  @PatchMapping("/api/v1/chauffeurs/{id}/statut")
  public ChauffeurResponse changerStatut(
      @PathVariable UUID id, @Valid @RequestBody StatutRequest request) {
    chauffeurService.changerStatut(id, request.valeur());
    return ChauffeurResponse.depuis(chauffeurService.consulterChauffeur(id));
  }

  @PatchMapping("/api/v1/chauffeurs/{id}/disponibilite")
  public ChauffeurResponse changerDisponibilite(
      @PathVariable UUID id, @Valid @RequestBody DisponibiliteRequest request) {
    chauffeurService.changerDisponibilite(id, request.valeur());
    return ChauffeurResponse.depuis(chauffeurService.consulterChauffeur(id));
  }

  private static ProfilChauffeur profil(ProfilChauffeurRequest request) {
    return request != null ? request.versProfil() : ProfilChauffeur.vide();
  }

  private static <T> List<T> habilitations(List<T> habilitations) {
    return habilitations != null ? habilitations : List.of();
  }
}
