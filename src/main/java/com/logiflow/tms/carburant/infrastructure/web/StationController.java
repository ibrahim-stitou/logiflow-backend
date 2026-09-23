package com.logiflow.tms.carburant.infrastructure.web;

import com.logiflow.tms.carburant.application.CarburantService;
import com.logiflow.tms.carburant.application.command.CreerStationCommand;
import com.logiflow.tms.carburant.application.command.MajStationCommand;
import com.logiflow.tms.carburant.infrastructure.web.dto.StationMajRequest;
import com.logiflow.tms.carburant.infrastructure.web.dto.StationRequest;
import com.logiflow.tms.carburant.infrastructure.web.dto.StationResponse;
import com.logiflow.tms.shared.application.PageRequest;
import com.logiflow.tms.shared.infrastructure.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class StationController {

  private final CarburantService carburantService;

  @PostMapping("/api/v1/stations")
  public ResponseEntity<StationResponse> creer(@Valid @RequestBody StationRequest request) {
    UUID id =
        carburantService.creerStation(
            new CreerStationCommand(request.code(), request.libelle(), request.adresse()));
    StationResponse reponse = StationResponse.depuis(carburantService.consulterStation(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/stations/{id}")
  public StationResponse consulter(@PathVariable UUID id) {
    return StationResponse.depuis(carburantService.consulterStation(id));
  }

  @GetMapping("/api/v1/stations")
  public PageResponse<StationResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats = carburantService.listerStations(q, new PageRequest(page, size));
    return PageResponse.of(resultats, StationResponse::depuis);
  }

  @PutMapping("/api/v1/stations/{id}")
  public StationResponse modifier(
      @PathVariable UUID id, @Valid @RequestBody StationMajRequest request) {
    carburantService.modifierStation(
        id, new MajStationCommand(request.libelle(), request.adresse()));
    return StationResponse.depuis(carburantService.consulterStation(id));
  }

  @DeleteMapping("/api/v1/stations/{id}")
  public ResponseEntity<Void> desactiver(@PathVariable UUID id) {
    carburantService.desactiverStation(id);
    return ResponseEntity.noContent().build();
  }
}
