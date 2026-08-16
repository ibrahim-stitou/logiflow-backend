package com.logiflow.tms.fleet.infrastructure.web;

import com.logiflow.tms.fleet.application.VehiculeService;
import com.logiflow.tms.fleet.application.command.CreerVehiculeCommand;
import com.logiflow.tms.fleet.application.command.MajVehiculeCommand;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeRequest;
import com.logiflow.tms.fleet.infrastructure.web.dto.VehiculeResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST du sous-domaine Véhicule. */
@RestController
@RequiredArgsConstructor
public class VehiculeController {

  private final VehiculeService vehiculeService;

  @PostMapping("/api/v1/vehicules")
  public ResponseEntity<VehiculeResponse> creer(@Valid @RequestBody VehiculeRequest request) {
    UUID id =
        vehiculeService.creerVehicule(
            new CreerVehiculeCommand(
                request.immatriculation(),
                request.type(),
                request.ptacKg(),
                request.chargeUtileKg(),
                request.documents()));
    VehiculeResponse reponse = VehiculeResponse.depuis(vehiculeService.consulterVehicule(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/vehicules/{id}")
  public VehiculeResponse consulter(@PathVariable UUID id) {
    return VehiculeResponse.depuis(vehiculeService.consulterVehicule(id));
  }

  @GetMapping("/api/v1/vehicules")
  public PageResponse<VehiculeResponse> lister(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var resultats = vehiculeService.listerVehicules(q, new PageRequest(page, size));
    return PageResponse.of(resultats, VehiculeResponse::depuis);
  }

  @PutMapping("/api/v1/vehicules/{id}/documents")
  public VehiculeResponse mettreAJourDocuments(
      @PathVariable UUID id, @Valid @RequestBody VehiculeRequest request) {
    vehiculeService.mettreAJourDocuments(id, new MajVehiculeCommand(request.documents()));
    return VehiculeResponse.depuis(vehiculeService.consulterVehicule(id));
  }

  @PutMapping("/api/v1/vehicules/{id}/compteurs")
  public VehiculeResponse relever(
      @PathVariable UUID id, @RequestParam int kilometrage, @RequestParam int heuresMoteur) {
    vehiculeService.relever(id, kilometrage, heuresMoteur);
    return VehiculeResponse.depuis(vehiculeService.consulterVehicule(id));
  }
}
