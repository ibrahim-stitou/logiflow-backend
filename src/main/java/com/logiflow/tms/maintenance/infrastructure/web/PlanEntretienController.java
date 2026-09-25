package com.logiflow.tms.maintenance.infrastructure.web;

import static com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.eur;
import static com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.montant;

import com.logiflow.tms.maintenance.application.OrdreTravailService;
import com.logiflow.tms.maintenance.application.PlanEntretienService;
import com.logiflow.tms.maintenance.application.PlanEntretienService.EcheancePlan;
import com.logiflow.tms.maintenance.domain.model.PlanEntretien;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.infrastructure.web.OrdreTravailController.OrdreTravailResponse;
import com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.EnginDto;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST des plans d'entretien et de leurs échéances. */
@RestController
@RequestMapping("/api/v1/maintenance/plans")
@RequiredArgsConstructor
public class PlanEntretienController {

  private final PlanEntretienService service;
  private final OrdreTravailService ordreTravailService;

  public record ParametresRequest(
      @NotBlank String libelle,
      TypeIntervention type,
      Integer periodiciteKm,
      Integer periodiciteMois,
      Integer periodiciteHeures,
      @PositiveOrZero Integer seuilAlerteKm,
      @PositiveOrZero Integer seuilAlerteJours,
      @PositiveOrZero Integer dureeEstimeeMin,
      @PositiveOrZero BigDecimal coutEstime,
      UUID prestataireId,
      Boolean actif) {

    PlanEntretien.Parametres versParametres() {
      return new PlanEntretien.Parametres(
          libelle,
          type,
          periodiciteKm,
          periodiciteMois,
          periodiciteHeures,
          seuilAlerteKm == null ? 0 : seuilAlerteKm,
          seuilAlerteJours == null ? 0 : seuilAlerteJours,
          dureeEstimeeMin == null ? 0 : dureeEstimeeMin,
          eur(coutEstime),
          prestataireId,
          actif == null || actif);
    }
  }

  public record CreerRequest(
      @NotNull @Valid EnginDto engin,
      @NotNull @Valid ParametresRequest parametres,
      LocalDate derniereDate,
      Integer derniereKm,
      Integer derniereHeures) {}

  public record EcheanceDto(
      Integer kmRestant,
      Integer heuresRestantes,
      LocalDate dateEcheance,
      String etat,
      double kmParJour,
      String immatriculation,
      Integer kilometrageActuel) {

    static EcheanceDto depuis(EcheancePlan e) {
      if (e == null) {
        return null;
      }
      return new EcheanceDto(
          e.echeance().kmRestant(),
          e.echeance().heuresRestantes(),
          e.echeance().dateEcheance(),
          e.echeance().etat().name(),
          Math.round(e.kmParJour() * 10) / 10.0,
          e.engin() == null ? null : e.engin().immatriculation(),
          e.engin() == null ? null : e.engin().kilometrage());
    }
  }

  public record PlanResponse(
      UUID id,
      EnginDto engin,
      String libelle,
      TypeIntervention type,
      Integer periodiciteKm,
      Integer periodiciteMois,
      Integer periodiciteHeures,
      int seuilAlerteKm,
      int seuilAlerteJours,
      int dureeEstimeeMin,
      BigDecimal coutEstime,
      UUID prestataireId,
      boolean actif,
      LocalDate derniereDate,
      Integer derniereKm,
      Integer derniereHeures,
      EcheanceDto echeance) {

    static PlanResponse depuis(PlanEntretien p, EcheancePlan echeance) {
      var params = p.parametres();
      var d = p.derniereRealisation();
      return new PlanResponse(
          p.id(),
          EnginDto.depuis(p.engin()),
          params.libelle(),
          params.type(),
          params.periodiciteKm(),
          params.periodiciteMois(),
          params.periodiciteHeures(),
          params.seuilAlerteKm(),
          params.seuilAlerteJours(),
          params.dureeEstimeeMin(),
          montant(params.coutEstime()),
          params.prestataireId(),
          params.actif(),
          d == null ? null : d.date(),
          d == null ? null : d.kilometrage(),
          d == null ? null : d.heures(),
          EcheanceDto.depuis(echeance));
    }
  }

  @PostMapping
  public ResponseEntity<PlanResponse> creer(@Valid @RequestBody CreerRequest r) {
    PlanEntretien plan =
        service.creer(
            r.engin().versRef(),
            r.parametres().versParametres(),
            r.derniereDate() == null
                ? null
                : new PlanEntretien.DerniereRealisation(
                    r.derniereDate(), r.derniereKm(), r.derniereHeures()));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(plan.id())
            .toUri();
    return ResponseEntity.created(location)
        .body(PlanResponse.depuis(plan, service.echeance(plan.id())));
  }

  @GetMapping
  public PageResponse<PlanResponse> lister(
      @RequestParam(required = false) TypeEngin typeEngin,
      @RequestParam(required = false) UUID enginId,
      @RequestParam(required = false) Boolean actif,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Map<UUID, EcheancePlan> echeances =
        service.echeances(null).stream().collect(Collectors.toMap(e -> e.plan().id(), e -> e));
    return PageResponse.of(
        service.rechercher(typeEngin, enginId, actif, new PageRequest(page, size)),
        p -> PlanResponse.depuis(p, echeances.get(p.id())));
  }

  /** Échéances de la flotte : échues, en alerte ou dans l'horizon (absent = tous les plans). */
  @GetMapping("/echeances")
  public List<PlanResponse> echeances(@RequestParam(required = false) Integer horizonJours) {
    return service.echeances(horizonJours).stream()
        .map(e -> PlanResponse.depuis(e.plan(), e))
        .toList();
  }

  @GetMapping("/{id}")
  public PlanResponse consulter(@PathVariable UUID id) {
    return PlanResponse.depuis(service.consulter(id), service.echeance(id));
  }

  @PutMapping("/{id}")
  public PlanResponse modifier(
      @PathVariable UUID id, @Valid @RequestBody ParametresRequest parametres) {
    service.modifier(id, parametres.versParametres());
    return consulter(id);
  }

  @GetMapping("/{id}/ordres-travail")
  public List<OrdreTravailResponse> historique(@PathVariable UUID id) {
    return ordreTravailService.parPlan(id).stream().map(OrdreTravailResponse::depuis).toList();
  }
}
