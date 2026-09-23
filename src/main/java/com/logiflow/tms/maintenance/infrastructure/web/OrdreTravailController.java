package com.logiflow.tms.maintenance.infrastructure.web;

import com.logiflow.tms.maintenance.application.MaintenanceService;
import com.logiflow.tms.maintenance.application.command.CreerOrdreTravailCommand;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.infrastructure.web.dto.OrdreTravailRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.OrdreTravailResponse;
import com.logiflow.tms.maintenance.infrastructure.web.dto.OrdreTravailStatsResponse;
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

@RestController
@RequiredArgsConstructor
public class OrdreTravailController {

  private final MaintenanceService maintenanceService;

  @PostMapping("/api/v1/ordres-travail")
  public ResponseEntity<OrdreTravailResponse> creer(
      @Valid @RequestBody OrdreTravailRequest request) {
    UUID id =
        maintenanceService.creerOrdreTravail(
            new CreerOrdreTravailCommand(
                request.vehiculeId(),
                request.type(),
                request.datePlanifiee(),
                request.coutEstime()));
    OrdreTravailResponse reponse =
        OrdreTravailResponse.depuis(maintenanceService.consulterOrdreTravail(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/ordres-travail/stats")
  public OrdreTravailStatsResponse stats(
      @RequestParam(required = false) UUID vehiculeId,
      @RequestParam(required = false) StatutOT statut) {
    return OrdreTravailStatsResponse.depuis(
        maintenanceService.statsOrdresTravail(vehiculeId, statut));
  }

  @GetMapping("/api/v1/ordres-travail")
  public PageResponse<OrdreTravailResponse> lister(
      @RequestParam(required = false) UUID vehiculeId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats =
        maintenanceService.listerOrdresTravail(vehiculeId, new PageRequest(page, size));
    return PageResponse.of(resultats, OrdreTravailResponse::depuis);
  }

  @GetMapping("/api/v1/ordres-travail/{id}")
  public OrdreTravailResponse consulter(@PathVariable UUID id) {
    return OrdreTravailResponse.depuis(maintenanceService.consulterOrdreTravail(id));
  }

  @PutMapping("/api/v1/ordres-travail/{id}/statut")
  public OrdreTravailResponse changerStatut(@PathVariable UUID id, @RequestParam StatutOT valeur) {
    maintenanceService.changerStatutOrdreTravail(id, valeur);
    return OrdreTravailResponse.depuis(maintenanceService.consulterOrdreTravail(id));
  }
}
