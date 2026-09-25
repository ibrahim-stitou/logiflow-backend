package com.logiflow.tms.maintenance.infrastructure.web;

import static com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.eur;
import static com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.montant;

import com.logiflow.tms.maintenance.application.OrdreTravailService;
import com.logiflow.tms.maintenance.application.SinistreService;
import com.logiflow.tms.maintenance.application.SinistreService.CoutsSinistre;
import com.logiflow.tms.maintenance.domain.model.GraviteSinistre;
import com.logiflow.tms.maintenance.domain.model.Responsabilite;
import com.logiflow.tms.maintenance.domain.model.Sinistre;
import com.logiflow.tms.maintenance.domain.model.StatutSinistre;
import com.logiflow.tms.maintenance.domain.model.TypeSinistre;
import com.logiflow.tms.maintenance.domain.port.out.SinistreRepository;
import com.logiflow.tms.maintenance.domain.vo.Tiers;
import com.logiflow.tms.maintenance.infrastructure.web.OrdreTravailController.OrdreTravailResponse;
import com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.DetailsOTRequest;
import com.logiflow.tms.maintenance.infrastructure.web.dto.MaintenanceDtos.EnginDto;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.vo.GeoPoint;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

/** API REST des sinistres : déclaration, suivi assurance, réparations et coût net. */
@RestController
@RequestMapping("/api/v1/maintenance/sinistres")
@RequiredArgsConstructor
public class SinistreController {

  private final SinistreService service;
  private final OrdreTravailService ordreTravailService;

  public record TiersDto(
      @NotBlank String nom, String immatriculation, String assureur, String numeroPolice) {}

  public record SinistreRequest(
      UUID vehiculeId,
      UUID remorqueId,
      UUID chauffeurId,
      UUID voyageId,
      @NotNull LocalDateTime dateSurvenance,
      String lieu,
      Double latitude,
      Double longitude,
      @NotNull TypeSinistre type,
      @NotNull GraviteSinistre gravite,
      Responsabilite responsabilite,
      @NotBlank String description,
      Boolean constatAmiable,
      Boolean rapportPolice,
      Boolean blesses,
      Boolean enginImmobilise,
      @Valid TiersDto tiers,
      UUID contratId,
      String numeroDossierAssureur,
      LocalDate dateDeclarationAssureur,
      UUID expertId,
      LocalDate dateExpertise,
      @PositiveOrZero BigDecimal estimationDommages,
      @PositiveOrZero BigDecimal franchise,
      @PositiveOrZero BigDecimal indemnite) {

    Sinistre.Circonstances circonstances() {
      return new Sinistre.Circonstances(
          vehiculeId,
          remorqueId,
          chauffeurId,
          voyageId,
          dateSurvenance,
          lieu,
          latitude == null || longitude == null ? null : new GeoPoint(latitude, longitude),
          type,
          gravite,
          responsabilite,
          description,
          Boolean.TRUE.equals(constatAmiable),
          Boolean.TRUE.equals(rapportPolice),
          Boolean.TRUE.equals(blesses),
          Boolean.TRUE.equals(enginImmobilise),
          tiers == null
              ? null
              : new Tiers(
                  tiers.nom(), tiers.immatriculation(), tiers.assureur(), tiers.numeroPolice()));
    }

    Sinistre.SuiviAssurance assurance() {
      return new Sinistre.SuiviAssurance(
          contratId,
          numeroDossierAssureur,
          dateDeclarationAssureur,
          expertId,
          dateExpertise,
          eur(estimationDommages),
          eur(franchise),
          eur(indemnite));
    }
  }

  public record StatutRequest(@NotNull StatutSinistre valeur) {}

  public record ReparationRequest(
      @NotNull @Valid EnginDto engin, @NotNull @Valid DetailsOTRequest details) {}

  public record CoutsDto(
      BigDecimal reparationsHt,
      BigDecimal reparationsTtc,
      BigDecimal indemnite,
      BigDecimal franchise,
      BigDecimal coutNet,
      int ordresTravail,
      int ordresOuverts) {

    static CoutsDto depuis(CoutsSinistre c) {
      return new CoutsDto(
          c.reparationsHt().montant(),
          c.reparationsTtc().montant(),
          c.indemnite().montant(),
          c.franchise().montant(),
          c.coutNet().montant(),
          c.ordresTravail(),
          c.ordresOuverts());
    }
  }

  public record SinistreResponse(
      UUID id,
      String reference,
      UUID vehiculeId,
      UUID remorqueId,
      UUID chauffeurId,
      UUID voyageId,
      LocalDateTime dateSurvenance,
      String lieu,
      Double latitude,
      Double longitude,
      TypeSinistre type,
      GraviteSinistre gravite,
      Responsabilite responsabilite,
      String description,
      boolean constatAmiable,
      boolean rapportPolice,
      boolean blesses,
      boolean enginImmobilise,
      TiersDto tiers,
      UUID contratId,
      String numeroDossierAssureur,
      LocalDate dateDeclarationAssureur,
      UUID expertId,
      LocalDate dateExpertise,
      BigDecimal estimationDommages,
      BigDecimal franchise,
      BigDecimal indemnite,
      StatutSinistre statut,
      LocalDate dateCloture,
      boolean declarationEnRetard,
      CoutsDto couts) {

    static SinistreResponse depuis(Sinistre s, CoutsSinistre couts) {
      var c = s.circonstances();
      var a = s.assurance();
      Tiers t = c.tiers();
      return new SinistreResponse(
          s.id(),
          s.reference().valeur(),
          c.vehiculeId(),
          c.remorqueId(),
          c.chauffeurId(),
          c.voyageId(),
          c.dateSurvenance(),
          c.lieu(),
          c.position() == null ? null : c.position().latitude(),
          c.position() == null ? null : c.position().longitude(),
          c.type(),
          c.gravite(),
          c.responsabilite(),
          c.description(),
          c.constatAmiable(),
          c.rapportPolice(),
          c.blesses(),
          c.enginImmobilise(),
          t == null
              ? null
              : new TiersDto(t.nom(), t.immatriculation(), t.assureur(), t.numeroPolice()),
          a.contratId(),
          a.numeroDossierAssureur(),
          a.dateDeclarationAssureur(),
          a.expertId(),
          a.dateExpertise(),
          montant(a.estimationDommages()),
          montant(a.franchise()),
          montant(a.indemnite()),
          s.statut(),
          s.dateCloture(),
          s.declarationEnRetard(LocalDate.now()),
          couts == null ? null : CoutsDto.depuis(couts));
    }
  }

  @PostMapping
  public ResponseEntity<SinistreResponse> declarer(@Valid @RequestBody SinistreRequest r) {
    Sinistre sinistre = service.declarer(r.circonstances(), r.assurance());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(sinistre.id())
            .toUri();
    return ResponseEntity.created(location).body(consulter(sinistre.id()));
  }

  @GetMapping
  public PageResponse<SinistreResponse> lister(
      @RequestParam(required = false) UUID enginId,
      @RequestParam(required = false) UUID chauffeurId,
      @RequestParam(required = false) StatutSinistre statut,
      @RequestParam(required = false) TypeSinistre type,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) LocalDate debut,
      @RequestParam(required = false) LocalDate fin,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var filtre =
        new SinistreRepository.Filtre(
            enginId,
            chauffeurId,
            statut,
            type,
            q,
            debut == null ? null : debut.atStartOfDay(),
            fin == null ? null : fin.plusDays(1).atStartOfDay());
    return PageResponse.of(
        service.rechercher(filtre, new PageRequest(page, size)),
        s -> SinistreResponse.depuis(s, service.couts(s.id())));
  }

  @GetMapping("/{id}")
  public SinistreResponse consulter(@PathVariable UUID id) {
    return SinistreResponse.depuis(service.consulter(id), service.couts(id));
  }

  @PutMapping("/{id}")
  public SinistreResponse modifier(@PathVariable UUID id, @Valid @RequestBody SinistreRequest r) {
    service.modifier(id, r.circonstances(), r.assurance());
    return consulter(id);
  }

  @PatchMapping("/{id}/statut")
  public SinistreResponse changerStatut(
      @PathVariable UUID id, @Valid @RequestBody StatutRequest request) {
    service.changerStatut(id, request.valeur());
    return consulter(id);
  }

  @GetMapping("/{id}/couts")
  public CoutsDto couts(@PathVariable UUID id) {
    return CoutsDto.depuis(service.couts(id));
  }

  @GetMapping("/{id}/ordres-travail")
  public List<OrdreTravailResponse> reparations(@PathVariable UUID id) {
    return ordreTravailService.parSinistre(id).stream().map(OrdreTravailResponse::depuis).toList();
  }

  @PostMapping("/{id}/reparations")
  public ResponseEntity<OrdreTravailResponse> creerReparation(
      @PathVariable UUID id, @Valid @RequestBody ReparationRequest r) {
    var ot = service.creerReparation(id, r.engin().versRef(), r.details().versDetails());
    URI location =
        ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/v1/maintenance/ordres-travail/{id}")
            .buildAndExpand(ot.id())
            .toUri();
    return ResponseEntity.created(location).body(OrdreTravailResponse.depuis(ot));
  }
}
