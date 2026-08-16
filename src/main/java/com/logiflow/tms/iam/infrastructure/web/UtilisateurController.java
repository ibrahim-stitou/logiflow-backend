package com.logiflow.tms.iam.infrastructure.web;

import com.logiflow.tms.iam.application.UtilisateurService;
import com.logiflow.tms.iam.application.command.CreerUtilisateurCommand;
import com.logiflow.tms.iam.application.command.MajUtilisateurCommand;
import com.logiflow.tms.iam.infrastructure.web.dto.UtilisateurRequest;
import com.logiflow.tms.iam.infrastructure.web.dto.UtilisateurResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
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

/** API REST du sous-domaine Utilisateur. */
@RestController
@RequiredArgsConstructor
public class UtilisateurController {

  private final UtilisateurService utilisateurService;

  @PostMapping("/api/v1/utilisateurs")
  public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurRequest request) {
    UUID id =
        utilisateurService.creerUtilisateur(
            new CreerUtilisateurCommand(request.login(), request.email(), request.roles()));
    UtilisateurResponse reponse =
        UtilisateurResponse.depuis(utilisateurService.consulterUtilisateur(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/utilisateurs/{id}")
  public UtilisateurResponse consulter(@PathVariable UUID id) {
    return UtilisateurResponse.depuis(utilisateurService.consulterUtilisateur(id));
  }

  @GetMapping("/api/v1/utilisateurs")
  public PageResponse<UtilisateurResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats = utilisateurService.listerUtilisateurs(q, new PageRequest(page, size));
    return PageResponse.of(resultats, UtilisateurResponse::depuis);
  }

  @PutMapping("/api/v1/utilisateurs/{id}")
  public UtilisateurResponse modifier(
      @PathVariable UUID id, @Valid @RequestBody UtilisateurRequest request) {
    utilisateurService.modifierUtilisateur(
        id, new MajUtilisateurCommand(request.email(), request.roles()));
    return UtilisateurResponse.depuis(utilisateurService.consulterUtilisateur(id));
  }

  @DeleteMapping("/api/v1/utilisateurs/{id}")
  public ResponseEntity<Void> desactiver(@PathVariable UUID id) {
    utilisateurService.desactiverUtilisateur(id);
    return ResponseEntity.noContent().build();
  }
}
