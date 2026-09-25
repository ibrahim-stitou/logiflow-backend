package com.logiflow.tms.ai.infrastructure.web;

import com.logiflow.tms.ai.application.MaintenancePredictiveService;
import com.logiflow.tms.ai.application.MaintenancePredictiveService.AnalyserMaintenanceCommand;
import com.logiflow.tms.ai.domain.model.maintenance.ResultatMaintenance;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Façade REST de l'agent de maintenance prédictive. Service IA indisponible → 503 : les scores et
 * ordres de travail existants restent consultables et saisissables à la main.
 */
@RestController
@RequiredArgsConstructor
public class MaintenancePredictiveController {

  private final MaintenancePredictiveService maintenancePredictiveService;

  /** Analyse d'un véhicule ou de toute la flotte ; enregistre les scores de santé par défaut. */
  public record AnalyseMaintenanceRequest(
      UUID vehiculeId, @Min(1) @Max(180) Integer horizonJours, Boolean enregistrerScores) {}

  @PostMapping("/api/v1/ia/maintenance/analyse")
  public ResultatMaintenance analyser(@Valid @RequestBody AnalyseMaintenanceRequest request) {
    return maintenancePredictiveService.analyser(
        new AnalyserMaintenanceCommand(
            request.vehiculeId(),
            request.horizonJours() == null ? 30 : request.horizonJours(),
            request.enregistrerScores() == null || request.enregistrerScores()));
  }
}
