package com.logiflow.tms.tracking.infrastructure.web;

import com.logiflow.tms.tracking.application.EvenementVoyageService;
import com.logiflow.tms.tracking.application.command.DeclarerEvenementCommand;
import com.logiflow.tms.tracking.infrastructure.web.dto.EvenementVoyageRequest;
import com.logiflow.tms.tracking.infrastructure.web.dto.EvenementVoyageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST du sous-domaine Suivi d'exécution. */
@RestController
@RequiredArgsConstructor
public class EvenementVoyageController {

  private final EvenementVoyageService evenementService;

  @PostMapping("/api/v1/evenements-voyage")
  public ResponseEntity<EvenementVoyageResponse> declarer(
      @Valid @RequestBody EvenementVoyageRequest request) {
    UUID id =
        evenementService.declarerEvenement(
            new DeclarerEvenementCommand(
                request.voyageId(),
                request.type(),
                request.horodatage(),
                request.position(),
                request.commentaire()));
    EvenementVoyageResponse reponse =
        EvenementVoyageResponse.depuis(evenementService.consulterEvenement(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/evenements-voyage/{id}")
  public EvenementVoyageResponse consulter(@PathVariable UUID id) {
    return EvenementVoyageResponse.depuis(evenementService.consulterEvenement(id));
  }

  @GetMapping("/api/v1/voyages/{voyageId}/evenements")
  public List<EvenementVoyageResponse> listerParVoyage(@PathVariable UUID voyageId) {
    return evenementService.listerParVoyage(voyageId).stream()
        .map(EvenementVoyageResponse::depuis)
        .toList();
  }
}
