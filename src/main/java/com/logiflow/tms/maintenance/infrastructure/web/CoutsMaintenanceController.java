package com.logiflow.tms.maintenance.infrastructure.web;

import com.logiflow.tms.maintenance.application.CoutsMaintenanceService;
import com.logiflow.tms.maintenance.application.CoutsMaintenanceService.CoutsMaintenance;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Tableau des coûts de maintenance et de la sinistralité sur une période. */
@RestController
@RequiredArgsConstructor
public class CoutsMaintenanceController {

  private final CoutsMaintenanceService service;

  /** Par défaut : les 12 derniers mois. */
  @GetMapping("/api/v1/maintenance/couts")
  public CoutsMaintenance couts(
      @RequestParam(required = false) LocalDate debut,
      @RequestParam(required = false) LocalDate fin,
      @RequestParam(required = false) TypeEngin typeEngin,
      @RequestParam(required = false) UUID enginId) {
    LocalDate f = fin == null ? LocalDate.now() : fin;
    LocalDate d = debut == null ? f.minusMonths(12).plusDays(1) : debut;
    if (d.isAfter(f)) {
      throw new BusinessException("Le début de la période doit précéder sa fin");
    }
    return service.couts(d, f, typeEngin, enginId);
  }
}
