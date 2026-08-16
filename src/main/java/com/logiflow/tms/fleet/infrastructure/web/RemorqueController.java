package com.logiflow.tms.fleet.infrastructure.web;

import com.logiflow.tms.fleet.application.RemorqueService;
import com.logiflow.tms.fleet.application.command.CreerRemorqueCommand;
import com.logiflow.tms.fleet.infrastructure.web.dto.RemorqueRequest;
import com.logiflow.tms.fleet.infrastructure.web.dto.RemorqueResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** API REST du sous-domaine Remorque. */
@RestController
@RequiredArgsConstructor
public class RemorqueController {

  private final RemorqueService remorqueService;

  @PostMapping("/api/v1/remorques")
  public ResponseEntity<RemorqueResponse> creer(@Valid @RequestBody RemorqueRequest request) {
    UUID id =
        remorqueService.creerRemorque(
            new CreerRemorqueCommand(
                request.immatriculation(),
                request.carrosserie(),
                request.volumeUtileM3(),
                request.nbPositionsPalettes(),
                request.chargeUtileKg(),
                request.groupeFroid(),
                request.temperatureMin(),
                request.temperatureMax()));
    RemorqueResponse reponse = RemorqueResponse.depuis(remorqueService.consulterRemorque(id));
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
    return ResponseEntity.created(location).body(reponse);
  }

  @GetMapping("/api/v1/remorques/{id}")
  public RemorqueResponse consulter(@PathVariable UUID id) {
    return RemorqueResponse.depuis(remorqueService.consulterRemorque(id));
  }
}
