package com.logiflow.tms.maintenance.infrastructure.web;

import com.logiflow.tms.maintenance.application.MaintenanceService;
import com.logiflow.tms.maintenance.application.command.CreerPlanEntretienCommand;
import com.logiflow.tms.maintenance.infrastructure.web.dto.PlanEntretienRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.PlanEntretienResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class PlanEntretienController {

  private final MaintenanceService maintenanceService;

  @PostMapping("/api/v1/plans-entretien")
  public ResponseEntity<PlanEntretienResponse> creer(
      @Valid @RequestBody PlanEntretienRequest request) {
    UUID id =
        maintenanceService.creerPlanEntretien(
            new CreerPlanEntretienCommand(
                request.vehiculeId(),
                request.libelle(),
                request.periodiciteKm(),
                request.periodiciteMois(),
                request.seuilAlerteKm(),
                request.dureeEstimeeMin()));
    PlanEntretienResponse reponse =
        PlanEntretienResponse.depuis(maintenanceService.consulterPlanEntretien(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/plans-entretien")
  public PageResponse<PlanEntretienResponse> lister(
      @RequestParam(required = false) UUID vehiculeId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats =
        maintenanceService.listerPlansEntretien(vehiculeId, new PageRequest(page, size));
    return PageResponse.of(resultats, PlanEntretienResponse::depuis);
  }

  @GetMapping("/api/v1/plans-entretien/{id}")
  public PlanEntretienResponse consulter(@PathVariable UUID id) {
    return PlanEntretienResponse.depuis(maintenanceService.consulterPlanEntretien(id));
  }
}
