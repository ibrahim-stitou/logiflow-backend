package com.logiflow.tms.maintenance.infrastructure.web;

import com.logiflow.tms.maintenance.application.MaintenanceService;
import com.logiflow.tms.maintenance.application.command.CalculerScoreSanteCommand;
import com.logiflow.tms.maintenance.infrastructure.web.dto.ScoreSanteRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.ScoreSanteResponse;
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
public class ScoreSanteController {

  private final MaintenanceService maintenanceService;

  @PostMapping("/api/v1/scores-sante")
  public ResponseEntity<ScoreSanteResponse> calculer(
      @Valid @RequestBody ScoreSanteRequest request) {
    UUID id =
        maintenanceService.calculerScoreSante(
            new CalculerScoreSanteCommand(
                request.vehiculeId(),
                request.score(),
                request.kmAvantEcheance(),
                request.dateEcheanceProjetee(),
                request.recommandation()));
    ScoreSanteResponse reponse =
        ScoreSanteResponse.depuis(maintenanceService.consulterScoreSante(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/scores-sante/dernier")
  public ScoreSanteResponse consulterDernier(@RequestParam UUID vehiculeId) {
    return maintenanceService
        .consulterDernierScoreSante(vehiculeId)
        .map(ScoreSanteResponse::depuis)
        .orElse(null);
  }

  @GetMapping(
      "/api/v1/scores-sante/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
  public ScoreSanteResponse consulter(@PathVariable UUID id) {
    return ScoreSanteResponse.depuis(maintenanceService.consulterScoreSante(id));
  }
}
