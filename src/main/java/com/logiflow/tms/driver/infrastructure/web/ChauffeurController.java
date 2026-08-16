package com.logiflow.tms.driver.infrastructure.web;

import com.logiflow.tms.driver.application.ChauffeurService;
import com.logiflow.tms.driver.application.command.CreerChauffeurCommand;
import com.logiflow.tms.driver.application.command.MajChauffeurCommand;
import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurRequest;
import com.logiflow.tms.driver.infrastructure.web.dto.ChauffeurResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
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

/** API REST du sous-domaine Chauffeur. */
@RestController
@RequiredArgsConstructor
public class ChauffeurController {

  private final ChauffeurService chauffeurService;

  @PostMapping("/api/v1/chauffeurs")
  public ResponseEntity<ChauffeurResponse> creer(@Valid @RequestBody ChauffeurRequest request) {
    UUID id =
        chauffeurService.creerChauffeur(
            new CreerChauffeurCommand(
                request.matricule(),
                request.nomComplet(),
                request.habilitations(),
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
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats = chauffeurService.listerChauffeurs(q, new PageRequest(page, size));
    return PageResponse.of(resultats, ChauffeurResponse::depuis);
  }

  @PutMapping("/api/v1/chauffeurs/{id}")
  public ChauffeurResponse modifier(
      @PathVariable UUID id, @Valid @RequestBody ChauffeurRequest request) {
    chauffeurService.modifierChauffeur(
        id, new MajChauffeurCommand(request.nomComplet(), request.habilitations()));
    return ChauffeurResponse.depuis(chauffeurService.consulterChauffeur(id));
  }
}
