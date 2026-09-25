package com.logiflow.tms.maintenance.infrastructure.web;

import static com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.eur;
import static com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.montant;

import com.logiflow.tms.maintenance.application.ContratAssuranceService;
import com.logiflow.tms.maintenance.domain.model.ContratAssurance;
import com.logiflow.tms.maintenance.domain.model.Garantie;
import com.logiflow.tms.maintenance.domain.model.TypeContrat;
import com.logiflow.tms.maintenance.domain.model.TypeEngin;
import com.logiflow.tms.maintenance.domain.vo.EnginRef;
import com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.EnginDto;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

/** API REST des contrats d'assurance. */
@RestController
@RequestMapping("/api/v1/maintenance/contrats-assurance")
@RequiredArgsConstructor
public class ContratAssuranceController {

  private final ContratAssuranceService service;

  public record ContratRequest(
      @NotNull UUID assureurId,
      @NotBlank String numeroPolice,
      @NotNull TypeContrat type,
      @NotEmpty Set<Garantie> garanties,
      @PositiveOrZero BigDecimal franchise,
      @PositiveOrZero BigDecimal primeAnnuelle,
      @NotNull LocalDate dateEffet,
      @NotNull LocalDate dateEcheance,
      List<@Valid EnginDto> engins,
      Boolean actif) {

    ContratAssurance.Conditions versConditions() {
      return new ContratAssurance.Conditions(
          assureurId,
          numeroPolice,
          type,
          garanties,
          eur(franchise),
          eur(primeAnnuelle),
          dateEffet,
          dateEcheance,
          engins == null ? List.of() : engins.stream().map(EnginDto::versRef).toList(),
          actif == null || actif);
    }
  }

  public record ContratResponse(
      UUID id,
      UUID assureurId,
      String numeroPolice,
      TypeContrat type,
      Set<Garantie> garanties,
      BigDecimal franchise,
      BigDecimal primeAnnuelle,
      LocalDate dateEffet,
      LocalDate dateEcheance,
      List<EnginDto> engins,
      boolean actif,
      boolean enVigueur) {

    static ContratResponse depuis(ContratAssurance contrat) {
      var c = contrat.conditions();
      LocalDate aujourdHui = LocalDate.now();
      return new ContratResponse(
          contrat.id(),
          c.assureurId(),
          c.numeroPolice(),
          c.type(),
          c.garanties(),
          montant(c.franchise()),
          montant(c.primeAnnuelle()),
          c.dateEffet(),
          c.dateEcheance(),
          c.engins().stream().map(EnginDto::depuis).toList(),
          c.actif(),
          c.actif()
              && !aujourdHui.isBefore(c.dateEffet())
              && !aujourdHui.isAfter(c.dateEcheance()));
    }
  }

  @PostMapping
  public ResponseEntity<ContratResponse> creer(@Valid @RequestBody ContratRequest r) {
    ContratAssurance contrat = service.creer(r.versConditions());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(contrat.id())
            .toUri();
    return ResponseEntity.created(location).body(ContratResponse.depuis(contrat));
  }

  @GetMapping
  public PageResponse<ContratResponse> lister(
      @RequestParam(required = false) Boolean actif,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return PageResponse.of(
        service.rechercher(actif, new PageRequest(page, size)), ContratResponse::depuis);
  }

  /** Contrat couvrant l'engin à la date (204 si aucun). */
  @GetMapping("/applicable")
  public ResponseEntity<ContratResponse> applicable(
      @RequestParam TypeEngin typeEngin,
      @RequestParam UUID enginId,
      @RequestParam(required = false) LocalDate date) {
    return service
        .applicable(new EnginRef(typeEngin, enginId), date == null ? LocalDate.now() : date)
        .map(c -> ResponseEntity.ok(ContratResponse.depuis(c)))
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/{id}")
  public ContratResponse consulter(@PathVariable UUID id) {
    return ContratResponse.depuis(service.consulter(id));
  }

  @PutMapping("/{id}")
  public ContratResponse modifier(@PathVariable UUID id, @Valid @RequestBody ContratRequest r) {
    return ContratResponse.depuis(service.modifier(id, r.versConditions()));
  }
}
