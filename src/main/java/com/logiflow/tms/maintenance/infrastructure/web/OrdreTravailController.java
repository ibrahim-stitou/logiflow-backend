package com.logiflow.tms.maintenance.infrastructure.web;

import static com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.montant;

import com.logiflow.tms.maintenance.application.OrdreTravailService;
import com.logiflow.tms.maintenance.application.OrdreTravailService.CreerOrdreTravail;
import com.logiflow.tms.maintenance.domain.model.NatureIntervention;
import com.logiflow.tms.maintenance.domain.model.OrdreTravail;
import com.logiflow.tms.maintenance.domain.model.OrigineOT;
import com.logiflow.tms.maintenance.domain.model.StatutOT;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.model.TypeIntervention;
import com.logiflow.tms.maintenance.domain.port.out.OrdreTravailRepository;
import com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.DetailsOTRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.EnginDto;
import com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.LigneCoutDto;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST des ordres de travail. */
@RestController
@RequestMapping("/api/v1/maintenance/ordres-travail")
@RequiredArgsConstructor
public class OrdreTravailController {

  private final OrdreTravailService service;

  public record CreerRequest(
      @NotNull @Valid EnginDto engin,
      OrigineOT origine,
      UUID planId,
      UUID sinistreId,
      @NotNull @Valid DetailsOTRequest details,
      List<@Valid LigneCoutDto> lignes) {}

  public record StatutRequest(@NotNull StatutOT valeur) {}

  public record ClotureRequest(
      @NotNull LocalDateTime finReelle,
      Integer kilometrage,
      Integer heures,
      String diagnostic,
      String travauxRealises,
      String intervenant,
      String numeroFacture,
      LocalDate dateFacture) {}

  public record OrdreTravailResponse(
      UUID id,
      String reference,
      EnginDto engin,
      OrigineOT origine,
      UUID planId,
      UUID sinistreId,
      TypeIntervention type,
      NatureIntervention nature,
      String priorite,
      String titre,
      String description,
      UUID prestataireId,
      LocalDateTime debutPlanifie,
      LocalDateTime finPlanifiee,
      boolean immobilisation,
      BigDecimal budgetEstime,
      StatutOT statut,
      List<LigneCoutDto> lignes,
      BigDecimal totalHt,
      BigDecimal totalTva,
      BigDecimal totalTtc,
      LocalDateTime debutReel,
      LocalDateTime finReelle,
      Long immobilisationHeures,
      Integer kilometrage,
      Integer heures,
      String diagnostic,
      String travauxRealises,
      String intervenant,
      String numeroFacture,
      LocalDate dateFacture) {

    public static OrdreTravailResponse depuis(OrdreTravail o) {
      var d = o.details();
      var r = o.realisation();
      return new OrdreTravailResponse(
          o.id(),
          o.reference().valeur(),
          EnginDto.depuis(o.engin()),
          o.origine(),
          o.planId(),
          o.sinistreId(),
          d.type(),
          d.nature(),
          d.priorite().name(),
          d.titre(),
          d.description(),
          d.prestataireId(),
          d.debutPlanifie(),
          d.finPlanifiee(),
          d.immobilisation(),
          montant(d.budgetEstime()),
          o.statut(),
          o.lignes().stream().map(LigneCoutDto::depuis).toList(),
          o.totalHt().montant(),
          o.totalTva().montant(),
          o.totalTtc().montant(),
          r.debutReel(),
          r.finReelle(),
          o.immobilisationHeures(),
          r.kilometrage(),
          r.heures(),
          r.diagnostic(),
          r.travauxRealises(),
          r.intervenant(),
          r.numeroFacture(),
          r.dateFacture());
    }
  }

  @PostMapping
  public ResponseEntity<OrdreTravailResponse> creer(@Valid @RequestBody CreerRequest request) {
    OrdreTravail ot =
        service.creer(
            new CreerOrdreTravail(
                request.engin().versRef(),
                request.origine(),
                request.planId(),
                request.sinistreId(),
                request.details().versDetails(),
                LigneCoutDto.versLignes(request.lignes())));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(ot.id())
            .toUri();
    return ResponseEntity.created(location).body(OrdreTravailResponse.depuis(ot));
  }

  @GetMapping
  public PageResponse<OrdreTravailResponse> lister(
      @RequestParam(required = false) TypeEngin typeEngin,
      @RequestParam(required = false) UUID enginId,
      @RequestParam(required = false) StatutOT statut,
      @RequestParam(required = false) TypeIntervention type,
      @RequestParam(required = false) NatureIntervention nature,
      @RequestParam(required = false) UUID prestataireId,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) LocalDate debut,
      @RequestParam(required = false) LocalDate fin,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var filtre =
        new OrdreTravailRepository.Filtre(
            typeEngin,
            enginId,
            statut,
            type,
            nature,
            prestataireId,
            q,
            debut == null ? null : debut.atStartOfDay(),
            fin == null ? null : fin.plusDays(1).atStartOfDay());
    return PageResponse.of(
        service.rechercher(filtre, new PageRequest(page, size)), OrdreTravailResponse::depuis);
  }

  @GetMapping("/{id}")
  public OrdreTravailResponse consulter(@PathVariable UUID id) {
    return OrdreTravailResponse.depuis(service.consulter(id));
  }

  @PutMapping("/{id}")
  public OrdreTravailResponse modifier(
      @PathVariable UUID id, @Valid @RequestBody DetailsOTRequest details) {
    return OrdreTravailResponse.depuis(service.modifier(id, details.versDetails()));
  }

  @PutMapping("/{id}/lignes")
  public OrdreTravailResponse remplacerLignes(
      @PathVariable UUID id, @RequestBody List<@Valid LigneCoutDto> lignes) {
    return OrdreTravailResponse.depuis(
        service.remplacerLignes(id, LigneCoutDto.versLignes(lignes)));
  }

  @PatchMapping("/{id}/statut")
  public OrdreTravailResponse changerStatut(
      @PathVariable UUID id, @Valid @RequestBody StatutRequest request) {
    return OrdreTravailResponse.depuis(service.changerStatut(id, request.valeur()));
  }

  @PostMapping("/{id}/cloture")
  public OrdreTravailResponse cloturer(
      @PathVariable UUID id, @Valid @RequestBody ClotureRequest r) {
    return OrdreTravailResponse.depuis(
        service.cloturer(
            id,
            new OrdreTravail.Cloture(
                r.finReelle(),
                r.kilometrage(),
                r.heures(),
                r.diagnostic(),
                r.travauxRealises(),
                r.intervenant(),
                r.numeroFacture(),
                r.dateFacture())));
  }
}
