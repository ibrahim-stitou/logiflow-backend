package com.logiflow.tms.carburant.infrastructure.web;

import com.logiflow.tms.carburant.application.CarburantService;
import com.logiflow.tms.carburant.application.command.CreerPriseCarburantCommand;
import com.logiflow.tms.carburant.application.command.MajPriseCarburantCommand;
import com.logiflow.tms.carburant.domain.model.PriseCarburant;
import com.logiflow.tms.carburant.domain.model.StatutPrise;
import com.logiflow.tms.carburant.domain.model.Station;
import com.logiflow.tms.carburant.infrastructure.web.dto.MajPriseCarburantRequest;
import com.logiflow.tms.carburant.infrastructure.web.dto.PriseCarburantRequest;
import com.logiflow.tms.carburant.infrastructure.web.dto.PriseCarburantResponse;
import com.logiflow.tms.carburant.infrastructure.web.dto.PriseCarburantStatsResponse;
import com.logiflow.tms.planning.api.VoyageApi;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.infrastructure.security.SecurityContextService;
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
public class PriseCarburantController {

  private final CarburantService carburantService;
  private final VoyageApi voyageApi;
  private final SecurityContextService securityContextService;

  @PostMapping("/api/v1/prises-carburant")
  public ResponseEntity<PriseCarburantResponse> creer(
      @Valid @RequestBody PriseCarburantRequest request) {
    UUID id =
        carburantService.creerPriseCarburant(
            new CreerPriseCarburantCommand(
                request.voyageId(),
                request.vehiculeId(),
                request.remorqueId(),
                request.stationId(),
                request.typeCarburant(),
                request.litrage(),
                request.montantTtc(),
                request.datePrise()));
    PriseCarburantResponse reponse = versReponse(carburantService.consulterPriseCarburant(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/prises-carburant/stats")
  public PriseCarburantStatsResponse stats(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) UUID voyageId,
      @RequestParam(required = false) StatutPrise statut) {
    return PriseCarburantStatsResponse.depuis(
        carburantService.statsPrisesCarburant(q, voyageId, statut));
  }

  @GetMapping("/api/v1/prises-carburant")
  public PageResponse<PriseCarburantResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) UUID voyageId,
      @RequestParam(required = false) StatutPrise statut,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats =
        carburantService.listerPrisesCarburant(q, voyageId, statut, new PageRequest(page, size));
    return PageResponse.of(resultats, this::versReponse);
  }

  @GetMapping("/api/v1/prises-carburant/{id}")
  public PriseCarburantResponse consulter(@PathVariable UUID id) {
    return versReponse(carburantService.consulterPriseCarburant(id));
  }

  @PutMapping("/api/v1/prises-carburant/{id}")
  public PriseCarburantResponse modifier(
      @PathVariable UUID id, @Valid @RequestBody MajPriseCarburantRequest request) {
    carburantService.modifierPriseCarburant(
        id,
        new MajPriseCarburantCommand(
            request.stationId(),
            request.typeCarburant(),
            request.litrage(),
            request.montantTtc()));
    return versReponse(carburantService.consulterPriseCarburant(id));
  }

  @PostMapping("/api/v1/prises-carburant/{id}/valider")
  public PriseCarburantResponse valider(@PathVariable UUID id) {
    verifierPeutValider();
    carburantService.validerPriseCarburant(id);
    return versReponse(carburantService.consulterPriseCarburant(id));
  }

  private void verifierPeutValider() {
    boolean peutValider =
        securityContextService
            .utilisateurCourant()
            .map(
                utilisateur ->
                    utilisateur.aLeRole("ROLE_EXPLOITANT")
                        || utilisateur.aLeRole("ROLE_RESPONSABLE_EXPLOITATION")
                        || utilisateur.aLeRole("ROLE_ADMINISTRATEUR"))
            .orElse(true);
    if (!peutValider) {
      throw new BusinessException("Seule l'exploitation peut valider une prise de carburant");
    }
  }

  private PriseCarburantResponse versReponse(PriseCarburant prise) {
    String voyageReference =
        voyageApi
            .consulter(prise.voyageId())
            .map(voyage -> voyage.reference())
            .orElse("—");
    Station station = carburantService.consulterStation(prise.stationId());
    return PriseCarburantResponse.depuis(
        prise, voyageReference, station.code(), station.libelle());
  }
}
