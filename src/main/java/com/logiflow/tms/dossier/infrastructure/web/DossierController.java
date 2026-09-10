package com.logiflow.tms.dossier.infrastructure.web;

import com.logiflow.tms.dossier.application.DossierTransportService;
import com.logiflow.tms.dossier.application.command.CreerDossierCommand;
import com.logiflow.tms.dossier.domain.model.StatutDossier;
import com.logiflow.tms.dossier.infrastructure.web.dto.DossierRequest;
import com.logiflow.tms.dossier.infrastructure.web.dto.DossierResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
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

/** API REST du sous-domaine Dossier de transport. */
@RestController
@RequiredArgsConstructor
public class DossierController {

  private final DossierTransportService dossierService;

  @PostMapping("/api/v1/dossiers")
  public ResponseEntity<DossierResponse> creer(@Valid @RequestBody DossierRequest request) {
    UUID id =
        dossierService.creerDossier(
            new CreerDossierCommand(
                request.commandeId(),
                request.typeTransport(),
                request.groupable(),
                request.nbPalettes(),
                request.familleMarchandise(),
                request.carrosserieRequise(),
                request.temperatureRequise(),
                request.lignesMarchandise(),
                request.segments(),
                request.documents() != null ? request.documents() : List.of()));
    DossierResponse reponse = DossierResponse.depuis(dossierService.consulterDossier(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/dossiers/{id}")
  public DossierResponse consulter(@PathVariable UUID id) {
    return DossierResponse.depuis(dossierService.consulterDossier(id));
  }

  @GetMapping("/api/v1/dossiers")
  public PageResponse<DossierResponse> lister(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) UUID commandeId) {
    if (commandeId != null) {
      var dossiers = dossierService.listerParCommande(commandeId);
      return PageResponse.ofList(
          dossiers.stream().map(DossierResponse::depuis).toList());
    }
    var resultats = dossierService.listerDossiers(new PageRequest(page, size));
    return PageResponse.of(resultats, DossierResponse::depuis);
  }

  @PutMapping("/api/v1/dossiers/{id}/statut")
  public DossierResponse changerStatut(@PathVariable UUID id, @RequestParam StatutDossier valeur) {
    dossierService.changerStatut(id, valeur);
    return DossierResponse.depuis(dossierService.consulterDossier(id));
  }
}
